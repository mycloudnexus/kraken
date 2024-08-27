import RequestMethod from "@/components/Method";
import { useGetRunningAPIList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IEnv, IRunningMapping } from "@/utils/types/env.type";
import { Flex, Table, Tag, Tooltip, Typography } from "antd";
import { useMemo, useState } from "react";
import MappingMatrix from "@/components/MappingMatrix";
import styles from "./index.module.scss"
import Text from '@/components/Text';
import { toDateTime } from '@/libs/dayjs';
type Props = {
  scrollHeight?: number;
  env?: IEnv;
};

interface GroupedItem {
  componentName?: string;
  items: IRunningMapping[];
  version: string;
}

const defaultPage = {
  size: 20,
  page: 0,
};

const RunningAPIMapping = ({ scrollHeight, env }: Props) => {
  const { currentProduct } = useAppStore();
  const [pageInfo] = useState(defaultPage);
  const { data, isLoading } = useGetRunningAPIList(
    currentProduct,
    {
      envId: env?.id,
      orderBy: "createdAt",
      direction: "DESC",
      ...pageInfo
    }
  );

  const columnData = useMemo((): GroupedItem[] => {
    const result = data as IRunningMapping[]
    if (!data) return [{
      componentName: '',
      items: [],
      version: ''
    }]
    const grouped = result.reduce((acc, item) => {
      const { componentName } = item;
      const group = acc.find(g => g.componentName === componentName);

      if (group) {
        group?.items?.push(item);
      } else {
        acc.push({
          componentName,
          items: [item],
          version: item.version
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
        <Flex gap={10}><Typography.Text>
          {item.componentName}
        </Typography.Text>
          {item.items.length > 0 && <Tag>
            <Text.LightMedium style={{ color: 'rgba(145, 86, 228, 1)' }}>
              {item?.items?.length}
            </Text.LightMedium>
          </Tag>}
        </Flex>)
    },
    {
      title: "Version",
      dataIndex: "version",
      width: 80
    },
    {
      dataIndex: "items",
      title: "API mappings",
      render: (items: Array<IRunningMapping>) => (
        <Flex vertical>
          {items.map((item: IRunningMapping, index: number) => (
            <Flex key={`${item.componentName}-${index}`} align="center" gap={10} className={styles.rowBorder}>
              <RequestMethod method={item?.method} />
              <Tooltip title={item?.path}>
                <span style={{ color: "#2962FF" }}>/{item?.path.split('/').slice(-2).join('/')}</span>
              </Tooltip>
              <Flex gap={8} align="center" flex={1}>
                <MappingMatrix
                  mappingMatrix={item?.mappingMatrix}
                  extraKey={"item.path"}
                  isItemActive={false}
                />
              </Flex>
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
            <Flex key={`${item.componentName}-${index}`} className={styles.rowBorder} justify='center' align='start' vertical>
              <Flex>
                {item?.userName ?? "-"}
              </Flex>
              <Flex>
                {toDateTime(item?.createAt)}
              </Flex>
            </Flex>
          ))}
        </Flex>)
    },
  ]

  const scroll = scrollHeight ? { y: scrollHeight - 152, x: "max-content" } : undefined
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
