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
  FilterFilled,
} from "@ant-design/icons";
import { Table, Flex, Button, DatePicker, Divider } from "antd";
import { ColumnsType, TableProps } from "antd/es/table";
import { FilterDropdownProps } from "antd/es/table/interface";
import dayjs, { Dayjs } from "dayjs";
import { omit } from "lodash";
import { Dispatch, SetStateAction, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import styles from "../../index.module.scss";

type EnvironmentActivityTablePropsType = {
  openActionModal: (requestId: string) => void;
  pathQuery: string;
};

const initPagination = {
  pageSize: DEFAULT_PAGING.size,
  current: DEFAULT_PAGING.page,
};

const { RangePicker } = DatePicker;

const TimeFilter = ({
  dates,
  setDates,
  handleTimeFilter,
  close,
  setIsTimeFiltered,
}: {
  dates: [Dayjs | null, Dayjs | null] | null;
  setDates: Dispatch<SetStateAction<[Dayjs | null, Dayjs | null] | null>>;
  handleTimeFilter: () => void;
  close: FilterDropdownProps["close"];
  setIsTimeFiltered: Dispatch<SetStateAction<boolean>>;
}) => {
  return (
    <div style={{ padding: "8px" }}>
      <Flex vertical>
        <Button
          type="text"
          className={styles.presetMenu}
          onClick={() => {
            setDates([dayjs().add(-7, "d"), dayjs()]);
          }}
        >
          Last Week
        </Button>
        <Button
          type="text"
          className={styles.presetMenu}
          onClick={() => {
            setDates([dayjs().add(-1, "month"), dayjs()]);
          }}
        >
          Last Month
        </Button>
        <span style={{ padding: "4px 10px" }}>Custom</span>
        <RangePicker
          placeholder={["From", "To"]}
          value={dates}
          onChange={(value) => {
            setDates(value);
          }}
        />
      </Flex>

      <Divider style={{ margin: "5px 0" }} />
      <Flex justify="flex-end" gap="small" style={{ margin: "5px 0" }}>
        <Button onClick={() => setDates(null)}>Reset</Button>
        <Button
          onClick={() => {
            handleTimeFilter();
            setIsTimeFiltered(!!dates);
            close();
          }}
          type="primary"
        >
          Ok
        </Button>
      </Flex>
    </div>
  );
};

const getStatusCodeWithIcon = (statusCode: number) => {
  switch (statusCode) {
    case 200:
    case 201:
    case 204:
      return (
        <span>
          <CheckCircleFilled className={styles["statusIcon-success"]} />
          {statusCode}
        </span>
      );
    case 400:
    case 401:
    case 403:
    case 500:
      return (
        <span>
          <CloseCircleFilled className={styles["statusIcon-error"]} />
          {statusCode}
        </span>
      );
    case 404:
    case 405:
    case 406:
    case 409:
    case 415:
    case 422:
      return (
        <span>
          <ExclamationCircleFilled className={styles["statusIcon-warn"]} />
          {statusCode}
        </span>
      );
    default:
      return <span>{statusCode}</span>;
  }
};

const EnvironmentActivityTable = (props: EnvironmentActivityTablePropsType) => {
  const { openActionModal, pathQuery } = props;
  const { currentProduct } = useAppStore();
  const { envId } = useParams();
  const [dates, setDates] = useState<[Dayjs | null, Dayjs | null] | null>(null);
  const [isTimeFiltered, setIsTimeFiltered] = useState(false);

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
      text: getStatusCodeWithIcon(200),
    },
    {
      value: 201,
      text: getStatusCodeWithIcon(201),
    },
    {
      value: 204,
      text: getStatusCodeWithIcon(204),
    },
    {
      value: 400,
      text: getStatusCodeWithIcon(400),
    },
    {
      value: 401,
      text: getStatusCodeWithIcon(401),
    },
    {
      value: 403,
      text: getStatusCodeWithIcon(403),
    },
    {
      value: 404,
      text: getStatusCodeWithIcon(404),
    },
    {
      value: 405,
      text: getStatusCodeWithIcon(405),
    },
    {
      value: 406,
      text: getStatusCodeWithIcon(406),
    },
    {
      value: 409,
      text: getStatusCodeWithIcon(409),
    },
    {
      value: 415,
      text: getStatusCodeWithIcon(415),
    },
    {
      value: 422,
      text: getStatusCodeWithIcon(422),
    },
    {
      value: 500,
      text: getStatusCodeWithIcon(500),
    },
  ];

  const handleTimeFilter = () => {
    setQueryParams({
      ...queryParams,
      requestStartTime: dates ? dayjs(dates[0]).startOf("day").valueOf() : null,
      requestEndTime: dates ? dayjs(dates[1]).endOf("day").valueOf() : null,
    });
  };

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
      render: (log: IActivityLog) => getStatusCodeWithIcon(log.httpStatusCode),
      filters: statusCodeOptions,
    },
    {
      key: "date",
      title: "Time",
      render: (log: IActivityLog) => toDateTime(log.createdAt),
      width: 200,
      filterDropdown: ({ close }) =>
        TimeFilter({
          dates,
          setDates,
          handleTimeFilter,
          close,
          setIsTimeFiltered,
        }),
      filterIcon: isTimeFiltered ? (
        <FilterFilled style={{ color: "#1677ff" }} />
      ) : (
        <FilterFilled />
      ),
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

  useEffect(() => {
    setQueryParams({
      ...queryParams,
      path: pathQuery,
    });
  }, [pathQuery]);

  const handleTableChange: TableProps<IActivityLog>["onChange"] = (
    pagination,
    filters
  ) => {
    setQueryParams({
      ...queryParams,
      page: (pagination.current ?? 1) - 1,
      size: pagination.pageSize,
      method: filters.name,
      statusCode: filters.status,
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
        y: 640,
        x: "max-content",
        scrollToFirstRowOnChange: false,
      }}
      onChange={handleTableChange}
    />
  );
};

export default EnvironmentActivityTable;
