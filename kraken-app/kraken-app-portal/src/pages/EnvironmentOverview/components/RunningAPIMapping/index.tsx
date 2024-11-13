import { ApiCard } from "@/components/ApiMapping";
import { Text } from "@/components/Text";
import { useGetRunningAPIList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IEnv, IRunningMapping } from "@/utils/types/env.type";
import { Flex, Table, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import { ColumnsType } from "antd/es/table";
import { toDateTime } from "@/libs/dayjs";
import styles from './index.module.scss'

type Props = {
  scrollHeight?: number;
  env?: IEnv;
};


const defaultPage = {
  size: 20,
  page: 0,
};

type GroupedMapping = IRunningMapping & { mappingCount: number }

const RunningAPIMapping = ({ scrollHeight, env }: Props) => {
  const { currentProduct } = useAppStore();
  const [pageInfo] = useState(defaultPage);
  const { data, isLoading } = useGetRunningAPIList(currentProduct, {
    envId: env?.id,
    orderBy: "createdAt",
    direction: "DESC",
    ...pageInfo,
  });

  const mappings = useMemo(() => {
    if (!data) return []

    const grouped: Record<string, GroupedMapping[]> = {}
    for (const mapping of data) {
      if (grouped[mapping.componentName] === undefined) {
        grouped[mapping.componentName] = []
      }

      grouped[mapping.componentName].push({ ...mapping, mappingCount: 0 })
    }

    return Object.values(grouped).flatMap(m => {
      m[0].mappingCount = m.length
      return m
    })
  }, [data]);

  const columns: ColumnsType<GroupedMapping> = [
    {
      title: "Component",
      width: 240,
      onCell: (item) => ({
        rowSpan: item.mappingCount,
      }),
      render: (item: GroupedMapping) => (
        <Flex gap={10}>
          <Typography.Text data-testid="componentName">{item.componentName}</Typography.Text>
          {item.mappingCount > 0 && (
            <Tag data-testid="componentCount" style={{ color: "var(--inprogress-text)", height: 'fit-content' }}>
              {item.mappingCount}
            </Tag>
          )}
        </Flex>
      ),
    },
    {
      title: "API mappings",
      render: (_, item) => (
        <ApiCard
          style={{ padding: 0 }}
          apiInstance={item}
        />
      ),
    },
    {
      title: "Version",
      width: 120,
      render: (item: GroupedMapping) => (
        <span data-testid="mappingVersion">{item.version}</span>
      ),
    },
    {
      title: "Created By",
      width: 200,
      render: (item: GroupedMapping) => (
        <Flex vertical gap={2}>
          <Text.LightMedium data-testid="createdBy">{item.userName}</Text.LightMedium>
          <Text.LightSmall data-testid="createdAt" color="#00000073">{toDateTime(item?.createAt)}</Text.LightSmall>
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
      dataSource={mappings}
      pagination={false}
      rowClassName={styles.mappingRow}
      rowKey={(item) => JSON.stringify(item)}
    />
  );
};

export default RunningAPIMapping;
