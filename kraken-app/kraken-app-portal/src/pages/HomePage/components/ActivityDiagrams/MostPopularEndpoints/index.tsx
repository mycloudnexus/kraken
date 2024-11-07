import RequestMethod from "@/components/Method";
import { Text } from "@/components/Text";
import TrimmedPath from "@/components/TrimmedPath";
import { useGetMostPopularEndpoints } from "@/hooks/homepage";
import { useAppStore } from "@/stores/app.store";
import { Flex, Progress, Spin, Tooltip } from "antd";
import Table, { ColumnsType } from "antd/es/table";
import { useEffect, useMemo } from "react";
import { DiagramProps } from "..";
import styles from "../index.module.scss";
import NoData from '../NoData';

type Props = {
  props: DiagramProps;
};

const MostPopularEndpoints = ({ props }: Props) => {
  const { currentProduct } = useAppStore();
  const { data, isLoading, refetch, isRefetching } = useGetMostPopularEndpoints(
    currentProduct,
    props.envId,
    props.startTime,
    props.endTime
  );

  useEffect(() => {
    refetch();
  }, [props]);

  const mostPopularEndpointsData = useMemo(
    () =>
      data?.endpointUsages.map((item, index) => ({
        key: index,
        ...item,
      })),
    [isLoading, data]
  );

  const columns: ColumnsType<any> = [
    {
      title: "#",
      dataIndex: "index",
      key: "index",
      render: (_, __, index) => <Text.LightSmall>{index + 1}</Text.LightSmall>,
      width: "5%",
    },
    {
      title: "Endpoint name",
      dataIndex: "endpoint",
      key: "endpoint",
      render: (_, record) => (
        <>
          <RequestMethod method={record.method} />
          <Tooltip title={record.endpoint}>
            .../
            <TrimmedPath path={record.endpoint} trimLevel={3} />
          </Tooltip>
        </>
      ),
      width: "50%",
    },
    {
      title: "Popularity",
      dataIndex: "popularity",
      key: "popularity",
      render: (popularity) => (
        <Progress
          percent={popularity}
          showInfo={false}
          strokeColor="#4096ff"
          trailColor="#e6f4ff"
        />
      ),
      width: "30%",
    },
    {
      title: "Usage",
      dataIndex: "usage",
      key: "usage",
      align: "right",
      render: (usage: number) => (
        <Text.LightSmall
          color="#2962FF"
          style={{
            border: "1px solid #2962FF",
            borderRadius: "8px",
            padding: "2px 8px",
          }}
        >
          {usage}
        </Text.LightSmall>
      ),
      width: "15%",
    },
  ];

  return (
    <Flex vertical className={styles.contentBox}>
      <Flex style={{ paddingBottom: "12px" }}>
        <Text.LightMedium>Most popular endpoints</Text.LightMedium>
      </Flex>
      <Flex
        className={styles.overflow}
        flex={1}
        justify="center"
        align="center"
      >
        <Spin spinning={isLoading || isRefetching}>
          {!mostPopularEndpointsData
            ? <NoData description='As endpoints are accessed, the most popular ones will be displayed here.' /> :
            <Table
              className={styles.table}
              columns={columns}
              dataSource={mostPopularEndpointsData}
              pagination={false}
              size="small"
              rowKey="endpoint"
            />
          }
        </Spin>
      </Flex>
    </Flex>
  );
};

export default MostPopularEndpoints;
