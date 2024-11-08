import { ApiCard } from "@/components/ApiMapping";
import { Text } from "@/components/Text";
import { useGetRunningAPIList } from "@/hooks/product";
import { toDateTime } from "@/libs/dayjs";
import { useAppStore } from "@/stores/app.store";
import { IEnv, IRunningMapping } from "@/utils/types/env.type";
import { Flex, Table, Tag, Typography } from "antd";
import { isEmpty } from "lodash";
import { useMemo, useState } from "react";
import styles from "./index.module.scss";

type Props = {
  scrollHeight?: number;
  env?: IEnv;
};

interface GroupedItem {
  componentName?: string;
  items: IRunningMapping[];
}

const defaultPage = {
  size: 20,
  page: 0,
};
export const ContentTime = ({ content = "", time = "" }) => {
  return (
    <Flex vertical gap={2}>
      <Text.LightMedium>{content}</Text.LightMedium>
      {!isEmpty(time) && (
        <Text.LightSmall color="#00000073">{time}</Text.LightSmall>
      )}
    </Flex>
  );
};

const RunningAPIMapping = ({ scrollHeight, env }: Props) => {
  const { currentProduct } = useAppStore();
  const [pageInfo] = useState(defaultPage);
  const { data, isLoading } = useGetRunningAPIList(currentProduct, {
    envId: env?.id,
    orderBy: "createdAt",
    direction: "DESC",
    ...pageInfo,
  });

  const columnData = useMemo((): GroupedItem[] => {
    const result = data as IRunningMapping[];
    if (!data)
      return [
        {
          componentName: "",
          items: [],
        },
      ];
    const grouped = result.reduce((acc, item) => {
      const { componentName } = item;
      const group = acc.find((g) => g.componentName === componentName);

      if (group) {
        group?.items?.push(item);
      } else {
        acc.push({
          componentName,
          items: [item],
        });
      }

      return acc;
    }, [] as GroupedItem[]);

    return grouped;
  }, [data]);

  const columns = [
    {
      title: "Component",
      dataIndex: "",
      render: (item: GroupedItem) => (
        <Flex gap={10}>
          <Typography.Text>{item.componentName}</Typography.Text>
          {item.items.length > 0 && (
            <Tag>
              <Text.LightMedium style={{ color: "rgba(145, 86, 228, 1)" }}>
                {item?.items?.length}
              </Text.LightMedium>
            </Tag>
          )}
        </Flex>
      ),
    },
    {
      dataIndex: "items",
      title: "API mappings",
      render: (items: Array<IRunningMapping>) => (
        <Flex vertical>
          {items.map((item: IRunningMapping, index: number) => (
            <ApiCard
              key={`${item.componentName}-${index}`}
              apiInstance={item}
            />
          ))}
        </Flex>
      ),
    },
    {
      title: "Version",
      dataIndex: "items",
      width: 90,
      render: (items: Array<IRunningMapping>) => (
        <Flex vertical>
          {items.map((item: IRunningMapping, index: number) => (
            <Flex
              key={`${item.version}-${index}`}
              justify="center"
              align="center"
              className={styles.rowBorder}
            >
              {item.version}
            </Flex>
          ))}
        </Flex>
      ),
    },
    {
      title: "Create By",
      dataIndex: "items",
      render: (items: Array<IRunningMapping>) => (
        <Flex vertical>
          {items.map((item: IRunningMapping, index: number) => (
            <Flex
              key={`${item.componentName}-${index}`}
              className={styles.rowBorder}
              justify="center"
              align="start"
              vertical
            >
              <ContentTime
                key={`${item.componentName}-${index}`}
                content={item?.userName}
                time={toDateTime(item?.createAt)}
              />
            </Flex>
          ))}
        </Flex>
      ),
    },
  ];

  const scroll = scrollHeight
    ? { y: scrollHeight - 144, x: "max-content" }
    : undefined;
  return (
    <Table
      scroll={scroll}
      columns={columns}
      loading={isLoading}
      dataSource={columnData}
      pagination={false}
      rowKey={(item) => JSON.stringify(item)}
    />
  );
};

export default RunningAPIMapping;
