import { useGetAuditLogs } from "@/hooks/product";
import { toDateTime } from "@/libs/dayjs";
import { DEFAULT_PAGING } from "@/utils/constants/common";
import { ILogActivity } from "@/utils/types/env.type";
import EmptyIcon from "@/assets/icon/empty.svg";
import {
  Button,
  DatePicker,
  Flex,
  Form,
  Input,
  Result,
  Table,
  Typography,
} from "antd";
import { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useState } from "react";
import ActivityDetailModal from "./components/AuditLogDetailsModal";
import styles from "./index.module.scss";
import { debounce, omit } from "lodash";

import dayjs from "dayjs";
import { SearchOutlined } from '@ant-design/icons';
import DeploymentStatus from '../EnvironmentOverview/components/DeploymentStatus';
import useUser from '@/hooks/user/useUser';
import { useCommonListProps } from '@/hooks/useCommonListProps';
const { RangePicker } = DatePicker;

const initPagination = {
  pageSize: DEFAULT_PAGING.size,
  current: DEFAULT_PAGING.page,
};

const AuditLog = () => {
  const { findUserIdByEmail } = useUser();
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
  } = useCommonListProps({ action: "UPDATE" }, initPagination);
  const { data, isLoading } = useGetAuditLogs(queryParams);

  const handleFormValues = useCallback(
    (values: any) => {
      const { requestTime = [] } = values ?? {};
      const params = omit(values, ["requestTime"]);
      params.requestStartTime = requestTime?.[0]
        ? dayjs(requestTime[0]).valueOf()
        : undefined;
      params.requestEndTime = requestTime?.[1]
        ? dayjs(requestTime[1]).valueOf()
        : undefined;

      if (!params.userId) {
        delete params.userId;
      }

      if (params.userEmail) {
        params.userId = findUserIdByEmail(params.userEmail) || ""
      }
      delete params.userEmail;
      setQueryParams(params);
    },
    [setQueryParams, findUserIdByEmail]
  );

  useEffect(() => {
    if (!isLoading) {
      const updatedTableData = data?.data;
      const updatedPagination = {
        current: data?.page ?? initPagination.current,
        pageSize: data?.size ?? initPagination.pageSize,
        total: data?.total ?? 0,
      };
      setPagination(updatedPagination);
      setTableData(updatedTableData);
    }
  }, [data, isLoading]);

  const debounceFn =
    debounce(() => {
      const formValue = form.getFieldsValue();
      handleFormValues(formValue);
    }, 2000)

  const handleFormValuesChange = useCallback(
    (t: any, values: any) => {
      if (t.userEmail) return;

      handleFormValues(values);
    },
    [handleFormValues]
  );


  const [modalActivity, setModalActivity] = useState<any>(undefined);
  const [modalOpen, setModalOpen] = useState(false);

  const columns: ColumnsType<ILogActivity> = [
    {
      key: "action",
      title: "Action type",
      render: (log: ILogActivity) => log.action,
    },
    {
      key: "resources",
      title: "Affected Resources",
      render: (log: ILogActivity) => log?.request?.metadata?.name,
    },
    {
      key: "userEmail",
      title: "User",
      render: (log: ILogActivity) => log.email,
    },
    {
      key: "date",
      title: "Time",
      render: (log: ILogActivity) => toDateTime(log.createdAt),
    },
    {
      key: "status",
      title: "Status",
      render: (log: ILogActivity) => <DeploymentStatus status={log.statusCode} />,
    },
    {
      key: "action",
      title: "",
      render: (log: ILogActivity) => (
        <Button
          type="link"
          onClick={() => {
            setModalActivity(log);
            setModalOpen(true);
          }}
        >
          View details
        </Button>
      ),
    },
  ];
  return (
    <div className={styles.root}>
      <Typography.Text style={{ fontSize: 16 }}>
        Audit log
      </Typography.Text>
      <div className={styles.tableWrapper}>
        <Flex align="center" className={styles.filterWrapper} >
          <Form
            form={form}
            layout="inline"
            colon={false}
            onValuesChange={handleFormValuesChange}
          >
            <Flex gap={12}>
              <Form.Item name={"userEmail"} noStyle>
                <Input
                  placeholder="Search user"
                  size="small"
                  suffix={<SearchOutlined style={{ cursor: "pointer" }} />}
                  onChange={debounceFn}
                />
              </Form.Item>

              <Form.Item label="Time range from" name="requestTime">
                <RangePicker
                  size="small"
                  placeholder={["Select time", "Select time"]}
                />
              </Form.Item>
            </Flex>

          </Form>
        </Flex>
        <Table
          dataSource={tableData}
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
            total: pagination?.total || 0,
            showSizeChanger: true,
            onShowSizeChange: handlePaginationShowSizeChange,
            showTotal: (total) => `Total ${total} items`,
          }}
          scroll={{ y: `calc(100vh - 310px)` }}
          locale={{ emptyText: (typeof queryParams.userId === "string" || !!queryParams.requestStartTime) ? (<Result subTitle="No matched audit log" icon={<EmptyIcon />}/>) : undefined }}
        />
      </div>

      <ActivityDetailModal
        item={modalActivity}
        open={modalOpen}
        onClose={() => setModalOpen(false)}
      />
    </div>
  );
};

export default AuditLog;
