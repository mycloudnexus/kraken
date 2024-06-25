import Text from "@/components/Text";
import {
  useGetComponentDetail,
  useCreateNewVersion,
  useGetVersionList,
} from "@/hooks/product";
import { DoubleLeftOutlined } from "@ant-design/icons";

import {
  Button,
  Divider,
  Flex,
  List,
  notification,
  Spin,
  Tabs,
  Typography,
} from "antd";
import clsx from "clsx";

import { useEffect, useMemo, useState } from "react";
import { showModalConfirmCreateVersion } from "./components/ModalConfirmCreateVersion";
import RenderList, { IMapProductAndType } from "./components/RenderList";
import styles from "./index.module.scss";
import { get, uniq } from "lodash";
import { useParams } from "react-router";
import { useAppStore } from "@/stores/app.store";
import { SUCCESS_CODE } from "@/utils/constants/api";
import ContactIcon from "@/assets/standardAPIMapping/contact.svg";

const listVersionDefault = [
  {
    key: "draft",
    label: "Draft version",
  },
];
const envAndVersion = [
  {
    env: "Dev",
    version: "0.10.0",
  },
  {
    env: "UAT",
    version: "0.10.0",
  },
  {
    env: "Stage",
  },
  {
    env: "Prod",
  },
];

const StandardAPIMapping = () => {
  const { currentProduct } = useAppStore();
  const { componentId } = useParams();
  const { data, isLoading } = useGetComponentDetail(
    currentProduct,
    componentId ?? ""
  );
  const { noTab, tabs } = useMemo(() => {
    if (isLoading) {
      return {
        noTab: true,
        tabs: [],
      };
    }
    const tabs: string[] = uniq(
      data?.facets?.supportedProductTypesAndActions?.reduce(
        (agg: string[], s: IMapProductAndType) => [
          ...agg,
          ...(s?.productTypes ?? []),
        ],
        []
      )
    );
    if (tabs.length === 0) {
      return {
        noTab: true,
        tabs: [],
      };
    }
    const tabWithInfo = tabs.map((tab: string) => ({
      name: tab,
      data: data?.facets?.supportedProductTypesAndActions?.filter(
        (s: IMapProductAndType) => s?.productTypes?.includes(tab)
      ),
    }));
    return {
      noTab: false,
      tabs: tabWithInfo,
    };
  }, [data, isLoading]);

  const [activeVersion, setActiveVersion] = useState("draft");
  const { mutateAsync: runCreateNewVersion } = useCreateNewVersion();
  const { data: versionData } = useGetVersionList(
    currentProduct,
    componentId ?? "",
    {
      page: 0,
      size: 100,
      componentKey: componentId,
    }
  );

  const listVersion = useMemo(() => {
    const listVersionData = get(versionData, "data", []).map((item) => ({
      key: get(item, "version", get(item, "id")),
      label: `Version ${get(item, "version", "")}`,
    }));
    return [...listVersionDefault, ...listVersionData];
  }, [versionData]);

  const handleCreateNewVersion = async () => {
    try {
      const data: any = {
        componentKey: componentId,
        productId: currentProduct,
        componentId,
      };
      const result = await runCreateNewVersion(data);
      if (+result.code !== SUCCESS_CODE) {
        throw new Error(result.message);
      }
      notification.success({ message: "Create new version success" });
    } catch (error) {
      notification.error({
        message: get(error, "message", "Error. Please try again"),
      });
    }
  };

  const [tab, setTab] = useState("");

  useEffect(() => {
    if (!noTab) {
      setTab("");
    }
    if (tabs.length) {
      setTab(tabs[0].name);
    }
  }, [noTab, tabs]);

  const componentName = get(data, "metadata.name", "");

  return (
    <Flex align="stretch" className={styles.pageWrapper}>
      <Flex vertical justify="space-between" className={styles.leftWrapper}>
        <Flex vertical gap={8}>
          <Flex vertical gap={8} className={styles.envWrapper}>
            <Flex align="center" gap={8}>
              <div className={styles.componentIconWrapper}>
                <ContactIcon />
              </div>
              <Typography.Text ellipsis={{ tooltip: true }}>
                {componentName}
              </Typography.Text>
            </Flex>
            <Flex gap={8} wrap="wrap">
              {envAndVersion.map((item) => (
                <Flex
                  key={item.env}
                  justify="space-between"
                  className={styles.envItem}
                >
                  <Typography.Text style={{ fontSize: 12 }}>
                    {item.env}
                  </Typography.Text>
                  <Typography.Text
                    style={{ fontSize: 12, color: "rgba(0,0,0,0.45)" }}
                  >
                    {item.version ?? "n/a"}
                  </Typography.Text>
                </Flex>
              ))}
            </Flex>
          </Flex>
          <List
            itemLayout="vertical"
            dataSource={listVersion}
            className={styles.list}
            renderItem={(item: { key: string; label: string }) => {
              const highlighted = activeVersion === item.key;
              return (
                <List.Item
                  key={item.key}
                  onClick={
                    !highlighted
                      ? () => {
                          setActiveVersion(item.key);
                        }
                      : undefined
                  }
                >
                  <div
                    className={clsx(styles.item, {
                      [styles.activeItem]: highlighted,
                    })}
                  >
                    {item.label}
                  </div>
                </List.Item>
              );
            }}
          />
        </Flex>
        <Flex
          vertical
          align="center"
          gap={8}
          className={styles.leftBottomWrapper}
        >
          <Text.NormalSmall color="#bfbfbf">Version 1.0</Text.NormalSmall>
          <Divider style={{ margin: 0 }} />
          <Button className={styles.switcherBtn}>
            <DoubleLeftOutlined />
          </Button>
        </Flex>
      </Flex>
      <Flex vertical gap={12} className={styles.mainWrapper}>
        <Flex align="center" justify="space-between">
          <Text.Custom size="20">Draft version</Text.Custom>
          <Button
            type="primary"
            onClick={() => {
              showModalConfirmCreateVersion({
                className: styles.modalCreate,
                onOk: handleCreateNewVersion,
              });
            }}
          >
            Create new version
          </Button>
        </Flex>
        <div className={styles.versionListWrapper}>
          <Spin spinning={isLoading}>
            {noTab ? (
              <RenderList
                data={data?.facets?.supportedProductTypesAndActions}
                componentId={componentId}
                tab={undefined}
              />
            ) : (
              <Tabs
                items={tabs.map(({ name, data }) => ({
                  key: name,
                  label: name,
                  children: (
                    <RenderList
                      data={data}
                      componentId={componentId}
                      tab={tab}
                    />
                  ),
                }))}
                onChange={(key) => setTab(key)}
              />
            )}
          </Spin>
        </div>
      </Flex>
    </Flex>
  );
};

export default StandardAPIMapping;
