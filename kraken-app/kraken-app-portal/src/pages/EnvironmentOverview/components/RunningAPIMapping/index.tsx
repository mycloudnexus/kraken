import { ApiCard } from "@/components/ApiMapping";
import { Text } from "@/components/Text";
import {useDisableApiUseCase, useGetRunningAPIList} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import {IApiUseCaseChangeHistory, IEnv, IRunningMapping} from "@/utils/types/env.type";
import {Flex, Switch, Table, Tag, Typography} from "antd";
import {useMemo, useState} from "react";
import { ColumnsType } from "antd/es/table";
import { toDateTime } from "@/libs/dayjs";
import styles from './index.module.scss'
import {HistoryOutlined} from "@ant-design/icons";
import {getAPIUscCaseChangeHistory} from "@/services/products.ts";
import FetchHistoryDrawer from "@/pages/EnvironmentOverview/components/ApiUseCaseChangeHistory";
import {DisableRequest} from "@/utils/types/product.type.ts";

type Props = {
  scrollHeight: number;
  env?: IEnv;
};

type GroupedMapping = IRunningMapping & { mappingCount: number }

const RunningAPIMapping = ({ scrollHeight, env }: Props) => {
  const { currentProduct } = useAppStore();
  const [drawerOpen, setDrawerOpen] = useState<boolean>(false);
  const [historyData, setHistoryData] = useState<IApiUseCaseChangeHistory[]>();
  const { data, isLoading } = useGetRunningAPIList(currentProduct, {
    envId: env?.id,
    orderBy: "createdAt",
    direction: "DESC",
  });
  const { mutateAsync: disableUseCase } = useDisableApiUseCase();
  const onStatusChange = (checked: boolean, item: GroupedMapping) => {
    disableUseCase({
      productId: currentProduct,
      mapperKey: item.targetMapperKey,
      envName: env?.name??'',
      checked,
      version: item.version,
    } as DisableRequest);
  }

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
      width: 400,
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
    {
      title: "Status",
      width: 120,
      render: (item: GroupedMapping) => (
          <Switch
              data-testid="disable-switch"
              defaultChecked={env?.name === 'stage'?item.stageAvailable:item.prodAvailable}
              onChange={(checked) => {
                onStatusChange(checked, item)}
          } />
      ),
    },
    {
      title: "History",
      width: 90,
      render: (item: GroupedMapping) => (
          <div
            data-testid="history-button"
            role={"button"}
            style={{cursor: 'pointer'}}
            onClick={async () => {
            await getAPIUscCaseChangeHistory(currentProduct, {mapperKey: item.targetMapperKey, env: env?.name}).then((items: {data: IApiUseCaseChangeHistory[]}) => {
              setHistoryData(items.data);
              setDrawerOpen(true)
            })
          }}>
            <HistoryOutlined />
          </div>
      ),
    },
  ];

  return (
    <>
    <Table
      scroll={{ y: scrollHeight, x: 800 }}
      columns={columns}
      loading={isLoading}
      dataSource={mappings}
      tableLayout="fixed"
      rowClassName={styles.mappingRow}
      rowKey={(item) => JSON.stringify(item)}
      pagination={false}
    />
    <FetchHistoryDrawer
      open={drawerOpen}
      data={historyData}
      onClose={() => setDrawerOpen(false)}
    />
    </>
  );
};

export default RunningAPIMapping;
