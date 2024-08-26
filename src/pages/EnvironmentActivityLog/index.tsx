import LogMethodTag from "@/components/LogMethodTag";

import { useGetProductEnvActivities, useGetProductEnvs } from "@/hooks/product";
import { useCommonListProps } from "@/hooks/useCommonListProps";
import { toDateTime } from "@/libs/dayjs";
import { useAppStore } from "@/stores/app.store";
import { DEFAULT_PAGING } from "@/utils/constants/common";
import { IActivityLog } from "@/utils/types/env.type";

import {
  Button,
  DatePicker,
  Flex,
  Form,
  Input,
  Select,
  Table,
  Space,
} from "antd";
import { ColumnsType } from "antd/es/table";

import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import ActivityDetailModal from "./components/ActivityDetailModal";
import styles from "./index.module.scss";
import _ from "lodash";

import dayjs from "dayjs";
const { RangePicker } = DatePicker;

const initPagination = {
  pageSize: DEFAULT_PAGING.size,
  current: DEFAULT_PAGING.page,
};
const methodOptions = [
  { value: "GET", label: "GET" },
  { value: "POST", label: "POST" },
  { value: "PATCH", label: "PATCH" },
  { value: "DELETE", label: "DELETE" },
];
const statusCodeOptions = [
  { value: 200, label: "200" },
  { value: 201, label: "201" },
  { value: 204, label: "204" },
  { value: 400, label: "400" },
  { value: 401, label: "401" },
  { value: 404, label: "404" },
  { value: 500, label: "500" },
];

const EnvironmentActivityLog = () => {
  const { envId } = useParams();
  const { currentProduct } = useAppStore();
  const { data: envData } = useGetProductEnvs(currentProduct);
  const [form] = Form.useForm();

  const {
    tableData,
    queryParams,
    setQueryParams,
    pagination,
    setPagination,
    setTableData,
    handlePaginationChange,
    handlePaginationShowSizeChange,
  } = useCommonListProps({}, initPagination);
  const { data, isLoading } = useGetProductEnvActivities(
    currentProduct,
    String(envId),
    queryParams
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

  const handleFormValues = useCallback(
    (values: any) => {
      const { requestTime = [] } = values ?? {};
      const params = _.omit(values, ["requestTime"]);
      params.requestStartTime = requestTime[0]
        ? dayjs(requestTime[0]).valueOf()
        : undefined;
      params.requestEndTime = requestTime[1]
        ? dayjs(requestTime[1]).valueOf()
        : undefined;

      if (!params.path) {
        delete params.path;
      }

      setQueryParams(params);
    },
    [setQueryParams]
  );

  const debounceFn = useCallback(
    _.debounce(() => {
      const formValue = form.getFieldsValue();
      handleFormValues(formValue);
    }, 2000),
    [form, handleFormValues]
  );

  const handleFormValuesChange = useCallback(
    (t: any, values: any) => {
      if (t.path) return;

      handleFormValues(values);
    },
    [handleFormValues]
  );

  const envOptions = useMemo(() => {
    return (
      envData?.data?.map((env) => ({
        value: env.id,
        label: env.name,
      })) ?? []
    );
  }, [envData]);

  const [modalActivityId, setModalActivityId] = useState<string | undefined>();
  const [modalOpen, setModalOpen] = useState(false);

  const columns: ColumnsType<IActivityLog> = [
    {
      key: "name",
      title: "API",
      render: (log: IActivityLog) => <LogMethodTag method={log.method} />,
    },
    {
      key: "name",
      title: "Path",
      render: (log: IActivityLog) => log.path,
    },
    {
      key: "status",
      title: "Status code",
      render: (log: IActivityLog) => log.httpStatusCode,
    },
    {
      key: "date",
      title: "Time",
      render: (log: IActivityLog) => toDateTime(log.createdAt),
    },
    {
      key: "action",
      title: "",
      render: (log: IActivityLog) => (
        <Button
          type="link"
          onClick={() => {
            setModalActivityId(log.requestId);
            setModalOpen(true);
          }}
        >
          View details
        </Button>
      ),
    },
  ];
  return (
    <div className={styles.wrapper}>
      <div className={styles.contentWrapper}>
        <Flex align="center" className={styles.filterWrapper}>
          <Form
            form={form}
            layout="inline"
            colon={false}
            onValuesChange={handleFormValuesChange}
          >
            <Form.Item label="Environment" name="envId">
              <Select
                options={envOptions}
                popupMatchSelectWidth={false}
                style={{ minWidth: 100 }}
                size="small"
                placeholder="All"
                allowClear
              />
            </Form.Item>
            <Form.Item label="Status code" name="statusCode">
              <Select
                options={statusCodeOptions}
                placeholder="All"
                popupMatchSelectWidth={false}
                size="small"
                style={{ minWidth: 100 }}
                allowClear
              />
            </Form.Item>

            <Form.Item label="Time range from" name="requestTime">
              <RangePicker
                size="small"
                placeholder={["Select time", "Select time"]}
              />
            </Form.Item>

            <Form.Item label="Method and path">
              <Space.Compact>
                <Form.Item name={"method"} noStyle>
                  <Select
                    options={methodOptions}
                    placeholder="All"
                    popupMatchSelectWidth={false}
                    size="small"
                    allowClear
                  />
                </Form.Item>
                <Form.Item name={"path"} noStyle>
                  <Input
                    placeholder="Input path"
                    size="small"
                    onChange={debounceFn}
                  />
                </Form.Item>
              </Space.Compact>
            </Form.Item>
          </Form>
        </Flex>
        <div className={styles.tableWrapper}>
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
            }}
            scroll={{ y: `calc(100vh - 310px)` }}
          />
        </div>
      </div>

      <ActivityDetailModal
        envId={String(envId)}
        activityId={modalActivityId ?? ""}
        open={modalOpen}
        setOpen={(value) => setModalOpen(value)}
      />
    </div>
  );
};

export default EnvironmentActivityLog;
