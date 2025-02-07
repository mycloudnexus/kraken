import { useGetPushActivityLogHistory } from "@/hooks/pushApiEvent";
import { useCommonListProps } from "@/hooks/useCommonListProps";
import useSize from "@/hooks/useSize";
import { useUser } from "@/hooks/user/useUser";
import { DEFAULT_PAGING } from "@/utils/constants/common";
import { DAY_FORMAT, DAY_TIME_FORMAT_NORMAL } from "@/utils/constants/format";
import { getStatusBadge } from "@/utils/helpers/ui";
import { IPushHistory } from "@/utils/types/env.type";
import { Badge, Flex, Table } from "antd";
import { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import { capitalize } from "lodash";
import { useEffect, useRef } from "react";
import styles from "../../index.module.scss";
import mockData from "./historyMockData.json";

const initPagination = {
  pageSize: DEFAULT_PAGING.size,
  current: DEFAULT_PAGING.page,
};

const PushHistoryList = () => {
  const {
    tableData,
    pagination,
    setPagination,
    setTableData,
    handlePaginationChange,
    handlePaginationShowSizeChange,
  } = useCommonListProps({}, initPagination);
  const { findUserName } = useUser();
  const { data, isLoading } = useGetPushActivityLogHistory();

  useEffect(() => {
    if (!isLoading) {
      const updatedTableData = data?.data ?? mockData.data.data;
      const updatedPagination = {
        current: data?.page ?? initPagination.current,
        pageSize: data?.size ?? initPagination.pageSize,
        total: data?.total,
      };
      setPagination(updatedPagination);
      setTableData(updatedTableData);
    }
  }, [data, isLoading]);

  const ref = useRef<any>();
  const size = useSize(ref);
  const refWrapper = useRef<any>();
  const sizeWrapper = useSize(refWrapper);

  const columns: ColumnsType<IPushHistory> = [
    {
      key: "createdAt",
      title: "Push time",
      render: (log: IPushHistory) =>
        dayjs(log.createdAt).format(DAY_TIME_FORMAT_NORMAL),
    },
    {
      key: "envName",
      title: "Environment",
      render: (log: IPushHistory) => capitalize(log.envName),
    },
    {
      title: "Time range",
      render: (log: IPushHistory) => (
        <>
          {dayjs(log.startTime).format(DAY_FORMAT)}
          <span style={{ padding: "0 10px", color: "rgba(0,0,0,0.45)" }}>
            to
          </span>
          {dayjs(log.endTime).format(DAY_FORMAT)}
        </>
      ),
    },
    {
      key: "pushedBy",
      title: "Pushed by",
      render: (log: IPushHistory) => findUserName(log.pushedBy),
    },
    {
      key: "status",
      title: "Status",
      render: (log: IPushHistory) => (
        <Flex gap={5}>
          <Badge status={getStatusBadge(log.status)} />
          {capitalize(log.status)}
        </Flex>
      ),
    },
  ];

  return (
    <Flex ref={refWrapper} style={{ height: "100%" }}>
      <Table
        dataSource={tableData}
        columns={columns}
        rowKey={(record) => `${record.id}_${record.createdAt}`}
        loading={isLoading}
        className={styles.table}
        pagination={{
          pageSize: pagination.pageSize,
          current: pagination.current + 1,
          onChange: handlePaginationChange,
          total: pagination.total,
          showSizeChanger: true,
          onShowSizeChange: handlePaginationShowSizeChange,
          showTotal: (total) => `Total ${total} items`,
        }}
        scroll={{
          y: (sizeWrapper?.height ?? 0) - (size?.height ?? 0) - 120,
        }}
      />
    </Flex>
  );
};

export default PushHistoryList;
