import Text from "@/components/Text";
import { useGetComponentDetail } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { DoubleLeftOutlined } from "@ant-design/icons";
import { Button, Divider, Flex, List, Spin, Tabs } from "antd";
import clsx from "clsx";
import { uniq } from "lodash";
import { useMemo, useState } from "react";
import { showModalConfirmCreateVersion } from "./components/ModalConfirmCreateVersion";
import RenderList, { IMapProductAndType } from "./components/RenderList";
import styles from "./index.module.scss";

const listVersion = [
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
  const { data, isLoading } = useGetComponentDetail(
    currentProduct,
    "mef.sonata.api.quote"
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
  return (
    <Flex align="stretch" className={styles.pageWrapper}>
      <Flex vertical justify="space-between" className={styles.leftWrapper}>
        <List
          itemLayout="vertical"
          dataSource={listVersion}
          className={styles.list}
          renderItem={(item) => {
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
          <Button type="primary">Create new version</Button>
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
              showModalConfirmCreateVersion();
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
            <RenderList data={data?.facets?.supportedProductTypesAndActions} />
          ) : (
            <Tabs
              items={tabs.map(({ name, data }) => ({
                key: name,
                label: name,
                children: <RenderList data={data} />,
              }))}
            />
          )}
        </Spin>
      </Flex>
    </Flex>
  );
};

export default StandardAPIMapping;
