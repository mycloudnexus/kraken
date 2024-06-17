import Text from "@/components/Text";
import {
  useGetComponentDetail,
  useCreateNewVersion,
  useGetVersionList,
} from "@/hooks/product";
import { DoubleLeftOutlined } from "@ant-design/icons";

import { Button, Divider, Flex, List, notification, Spin, Tabs } from "antd";
import clsx from "clsx";

import { useEffect, useMemo, useState } from "react";
import { showModalConfirmCreateVersion } from "./components/ModalConfirmCreateVersion";
import RenderList, { IMapProductAndType } from "./components/RenderList";
import styles from "./index.module.scss";
import { get, uniq } from "lodash";
import { useParams } from "react-router";
import { useAppStore } from "@/stores/app.store";
import { SUCCESS_CODE } from "@/utils/constants/api";

const listVersionDefault = [
  {
    key: "current",
    label: "Current Mapping",
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
    env: "Production",
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

  const [activeVersion, setActiveVersion] = useState("current");
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

  return (
    <Flex align="stretch" className={styles.pageWrapper}>
      <Flex vertical justify="space-between" className={styles.leftWrapper}>
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
          <Text.Custom size="20">Standard API Mapping</Text.Custom>
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
        <Flex align="center" justify="space-between">
          <Flex align="center" gap={24}>
            <Text.NormalLarge>Current configure</Text.NormalLarge>
            <Text.NormalMedium>
              Created at: 2024-05-15 04:34:56
            </Text.NormalMedium>
            <Text.NormalMedium>By user name</Text.NormalMedium>
          </Flex>
          <Flex align="center" gap={16}>
            {envAndVersion.map((item) => (
              <Flex key={item.env} gap={4} style={{ padding: "4px 8px" }}>
                <Text.NormalSmall>{item.env}</Text.NormalSmall>
                <Text.NormalSmall color="rgba(0,0,0,0.45)">
                  {item.version ?? "n/a"}
                </Text.NormalSmall>
              </Flex>
            ))}
          </Flex>
        </Flex>
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
                  <RenderList data={data} componentId={componentId} tab={tab} />
                ),
              }))}
              onChange={(key) => setTab(key)}
            />
          )}
        </Spin>
      </Flex>
    </Flex>
  );
};

export default StandardAPIMapping;
