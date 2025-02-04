import { useGetProductEnvActivities } from "@/hooks/product";
import { useCommonListProps } from "@/hooks/useCommonListProps";
import { useAppStore } from "@/stores/app.store";
import { DEFAULT_PAGING } from "@/utils/constants/common";
import { IActivityLog } from "@/utils/types/env.type";
import { Table } from "antd";
import { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import { omit } from "lodash";
import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { Size } from "recharts/types/util/types";
import styles from "../../index.module.scss";

type EnvironmentActivityTablePropsType = {
  columns: ColumnsType<IActivityLog>;
  size?: Size;
  sizeWrapper?: Size;
};

const initPagination = {
  pageSize: DEFAULT_PAGING.size,
  current: DEFAULT_PAGING.page,
};

const EnvironmentActivityTable = (props: EnvironmentActivityTablePropsType) => {
  const { columns, size, sizeWrapper } = props;
  const { currentProduct } = useAppStore();
  const { envId } = useParams();

  const {
    tableData,
    pagination,
    queryParams,
    setPagination,
    setTableData,
    handlePaginationChange,
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
        onChange: handlePaginationChange,
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
    />
  );
};

export default EnvironmentActivityTable;
