import { PageLayout } from "@/components/Layout";
import LogMethodTag from "@/components/LogMethodTag";
import { useGetProductEnvActivities, useGetProductEnvs } from "@/hooks/product";
import { useCommonListProps } from "@/hooks/useCommonListProps";
import useSize from "@/hooks/useSize";
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
  Tabs,
} from "antd";
import { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import { debounce, omit } from "lodash";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import ActivityDetailModal from "./components/ActivityDetailModal";
import styles from "./index.module.scss";
import { useGetPushActivityLogHistory } from '@/hooks/pushApiEvent';
import PushHistoryModal from './components/PushHistoryModal';
import { useBoolean } from 'usehooks-ts';

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
  const { data: envData, isLoading: loadingEnv } =
    useGetProductEnvs(currentProduct);
  const { data: pushData, isLoading: loadingPushHistory } =
    useGetPushActivityLogHistory();
  const [form] = Form.useForm();
  const ref = useRef<any>();
  const size = useSize(ref);
  const refWrapper = useRef<any>();
  const sizeWrapper = useSize(refWrapper);
  const [mainTabKey, setMainTabKey] = useState<string>('activityLog');
  const { value: isOpen, setTrue: open, setFalse: close } = useBoolean(false);

  useEffect(() => {
    console.log(pushData)
  }, [loadingPushHistory])

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
  const envActivityParams = {
    productId: currentProduct,
    envId: queryParams?.envId || String(envId),
    params: omit(queryParams, ["envId"])
  }
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

  const handleFormValues = useCallback(
    (values: any) => {
      const { requestTime = [] } = values ?? {};
      const params = omit(values, ["requestTime"]);
      params.requestStartTime = requestTime?.[0]
        ? dayjs(requestTime[0]).startOf("day").valueOf()
        : undefined;
      params.requestEndTime = requestTime?.[1]
        ? dayjs(requestTime[1]).endOf("day").valueOf()
        : undefined;

      if (!params.path) {
        delete params.path;
      }

      setQueryParams(params);
    },
    [setQueryParams]
  );

  const debounceFn = useCallback(
    debounce(() => {
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
  const isActivityLogActive = useMemo(() => mainTabKey === 'activityLog', [mainTabKey])

  const columns: ColumnsType<IActivityLog> = [
    {
      key: "name",
      title: "Method",
      render: (log: IActivityLog) => <LogMethodTag method={log.method} />,
      width: 120,
    },
    {
      key: "name",
      title: "Path",
      render: (log: IActivityLog) => log.path,
    },
    {
      key: "buyerName",
      title: "Buyer name",
      render: (log: IActivityLog) => log.buyerName,
    },
    {
      key: "status",
      title: "Status code",
      width: 160,
      render: (log: IActivityLog) => log.httpStatusCode,
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
      width: 200,
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
    <PageLayout title="API activity log">
      <div className={styles.contentWrapper} ref={refWrapper}>
        {isOpen && (
          <PushHistoryModal
            isOpen={isOpen}
            queryParams={envActivityParams}
            envOptions={envOptions}
            onClose={close}
            onOK={() => alert('OK')}
          />
        )}
        <Flex align="center" justify="space-between">
          <Tabs
            id="tab-mapping"
            activeKey={mainTabKey}
            onChange={setMainTabKey}
            items={[
              {
                label: 'Activity log',
                key: 'activityLog',
              },
              {
                label: "Push history",
                key: 'pushHistory'
              },
            ]}
          />
          {isActivityLogActive && <Button type='primary' onClick={open}>
            Push log
          </Button>}
        </Flex>

        <Flex align="center" className={styles.filterWrapper} ref={ref}>
          <Form
            initialValues={{ envId }}
            style={{ gap: 5 }}
            form={form}
            layout="inline"
            colon={false}
            onValuesChange={handleFormValuesChange}
          >
            <Form.Item label="Environment" name="envId">
              <Select
                loading={loadingEnv}
                options={envOptions}
                popupMatchSelectWidth={false}
                style={{ minWidth: 100, maxWidth: 120 }}
                placeholder="All"
              />
            </Form.Item>
            <Form.Item label="Status code" name="statusCode">
              <Select
                options={statusCodeOptions}
                placeholder="All"
                popupMatchSelectWidth={false}
                style={{ minWidth: 100 }}
                allowClear
              />
            </Form.Item>

            <Form.Item label="Time range from" name="requestTime">
              <RangePicker
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
                    allowClear
                  />
                </Form.Item>
                <Form.Item name={"path"} noStyle>
                  <Input
                    className={styles.inputPath}
                    placeholder="Input path"
                    onChange={debounceFn}
                  />
                </Form.Item>
              </Space.Compact>
            </Form.Item>
          </Form>
        </Flex>
        <div className={styles.tableWrapper}>
          {!isLoading && isActivityLogActive ? <Table
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
            scroll={{
              y: (sizeWrapper?.height ?? 0) - (size?.height ?? 0) - 120,
            }}
          />
            : <div></div>
          }
        </div>
      </div>

      <ActivityDetailModal
        envId={String(envId)}
        activityId={modalActivityId ?? ""}
        open={modalOpen}
        setOpen={(value) => setModalOpen(value)}
      />
    </PageLayout>
  );
};

export default EnvironmentActivityLog;
