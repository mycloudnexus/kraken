import LogMethodTag from "@/components/LogMethodTag";
import TrimmedPath from "@/components/TrimmedPath";
import { useGetProductEnvActivities } from "@/hooks/product";
import { useCommonListProps } from "@/hooks/useCommonListProps";
import { toDateTime } from "@/libs/dayjs";
import { useAppStore } from "@/stores/app.store";
import { DEFAULT_PAGING } from "@/utils/constants/common";
import { IActivityLog } from "@/utils/types/env.type";
import {
  CheckCircleFilled,
  ExclamationCircleFilled,
  CloseCircleFilled,
} from "@ant-design/icons";
import { Table, Flex, Button } from "antd";
import { ColumnsType, TableProps } from "antd/es/table";
import dayjs from "dayjs";
import { omit } from "lodash";
import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { Size } from "recharts/types/util/types";
import styles from "../../index.module.scss";

type EnvironmentActivityTablePropsType = {
  openActionModal: (requestId: string) => void;
  size?: Size;
  sizeWrapper?: Size;
};

const initPagination = {
  pageSize: DEFAULT_PAGING.size,
  current: DEFAULT_PAGING.page,
};

const EnvironmentActivityTable = (props: EnvironmentActivityTablePropsType) => {
  const { openActionModal, size, sizeWrapper } = props;
  const { currentProduct } = useAppStore();
  const { envId } = useParams();

  const {
    tableData,
    pagination,
    queryParams,
    setQueryParams,
    setPagination,
    setTableData,
    handlePaginationShowSizeChange,
  } = useCommonListProps({}, initPagination);

  const envActivityParams = {
    productId: currentProduct,
    envId: queryParams?.envId || String(envId),
    params: omit(queryParams, ["envId"]),
  };

  const { data, isLoading } = useGetProductEnvActivities(
    envActivityParams.productId,
    envActivityParams.envId,
    envActivityParams.params
  );

  const methodOptions = [
    {
      text: "POST",
      value: "POST",
    },
    {
      text: "PUT",
      value: "PUT",
    },
    {
      text: "GET",
      value: "GET",
    },
    {
      text: "PATCH",
      value: "PATCH",
    },
    {
      text: "DELETE",
      value: "DELETE",
    },
  ];

  const statusCodeOptions = [
    {
      value: 200,
      text: (
        <span>
          <CheckCircleFilled className={styles["statusIcon-success"]} />
          200
        </span>
      ),
    },
    {
      value: 201,
      text: (
        <span>
          <CheckCircleFilled className={styles["statusIcon-success"]} />
          201
        </span>
      ),
    },
    {
      value: 204,
      text: (
        <span>
          <CheckCircleFilled className={styles["statusIcon-success"]} />
          204
        </span>
      ),
    },
    {
      value: 400,
      text: (
        <span>
          <CloseCircleFilled className={styles["statusIcon-error"]} />
          400
        </span>
      ),
    },
    {
      value: 401,
      text: (
        <span>
          <CloseCircleFilled className={styles["statusIcon-error"]} />
          401
        </span>
      ),
    },
    {
      value: 403,
      text: (
        <span>
          <CloseCircleFilled className={styles["statusIcon-error"]} />
          403
        </span>
      ),
    },
    {
      value: 404,
      text: (
        <span>
          <ExclamationCircleFilled className={styles["statusIcon-warn"]} />
          404
        </span>
      ),
    },
    {
      value: 405,
      text: (
        <span>
          <ExclamationCircleFilled className={styles["statusIcon-warn"]} />
          405
        </span>
      ),
    },
    {
      value: 406,
      text: (
        <span>
          <ExclamationCircleFilled className={styles["statusIcon-warn"]} />
          406
        </span>
      ),
    },
    {
      value: 409,
      text: (
        <span>
          <ExclamationCircleFilled className={styles["statusIcon-warn"]} />
          409
        </span>
      ),
    },
    {
      value: 415,
      text: (
        <span>
          <ExclamationCircleFilled className={styles["statusIcon-warn"]} />
          415
        </span>
      ),
    },
    {
      value: 422,
      text: (
        <span>
          <ExclamationCircleFilled className={styles["statusIcon-warn"]} />
          422
        </span>
      ),
    },
    {
      value: 500,
      text: (
        <span>
          <CloseCircleFilled className={styles["statusIcon-error"]} />
          500
        </span>
      ),
    },
  ];

  const columns: ColumnsType<IActivityLog> = [
    {
      key: "name",
      title: "Method",
      render: (log: IActivityLog) => <LogMethodTag method={log.method} />,
      width: 100,
      filters: methodOptions,
    },
    {
      key: "name",
      title: "Path",
      width: 300,
      render: (log: IActivityLog) => (
        <Flex>
          <TrimmedPath path={log.path} />
        </Flex>
      ),
    },
    {
      key: "buyerName",
      title: "Buyer name",
      width: 200,
      render: (log: IActivityLog) => log.buyerName,
    },
    {
      key: "status",
      title: "Status code",
      width: 140,
      render: (log: IActivityLog) => log.httpStatusCode,
      filters: statusCodeOptions,
      onFilter: (value, record) => record.httpStatusCode === value,
    },
    {
      key: "date",
      title: "Time",
      render: (log: IActivityLog) => toDateTime(log.createdAt),
      width: 200,
    },
    {
      key: "action",
      title: "Action",
      width: 160,
      fixed: "right",
      render: (log: IActivityLog) => (
        <Button
          type="link"
          onClick={() => {
            openActionModal(log.requestId);
          }}
        >
          View details
        </Button>
      ),
    },
  ];

  useEffect(() => {
    if (!isLoading) {
      const updatedTableData = data?.data;
      const updatedPagination = {
        current: data?.page ?? initPagination.current,
        pageSize: data?.size ?? initPagination.pageSize,
        total: data?.total,
      };
      setPagination(updatedPagination);
      setTableData(updatedTableData!);
    }
  }, [data, isLoading]);

  const handleTableChange: TableProps<IActivityLog>["onChange"] = (
    pagination,
    filters
  ) => {
    setQueryParams({
      ...queryParams,
      page: (pagination.current ?? 1) - 1,
      size: pagination.pageSize,
      ...filters,
    });
  };

  return (
    <Table
      dataSource={[...tableData]?.sort(
        (a: any, b: any) =>
          dayjs(b.createdAt).valueOf() - dayjs(a.createdAt).valueOf()
      )}
      columns={columns}
      rowKey={(record) =>
        `${record.method}_${record.requestId}_${record.createdAt}`
      }
      loading={isLoading}
      className={styles.table}
      rowClassName={styles.hovering}
      pagination={{
        pageSize: pagination.pageSize,
        current: pagination.current + 1,
        total: pagination.total,
        showSizeChanger: true,
        onShowSizeChange: handlePaginationShowSizeChange,
        showTotal: (total) => `Total ${total} items`,
        showQuickJumper: true,
      }}
      scroll={{
        y: (sizeWrapper?.height ?? 0) - (size?.height ?? 0) - 120,
        x: 800,
      }}
      onChange={handleTableChange}
    />
  );
};

export default EnvironmentActivityTable;
