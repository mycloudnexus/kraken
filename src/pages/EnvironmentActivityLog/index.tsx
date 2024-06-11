import Text from "@/components/Text";
import { useGetProductEnvActivities, useGetProductEnvs } from "@/hooks/product";
import { useCommonListProps } from "@/hooks/useCommonListProps";
import { toDateTime } from "@/libs/dayjs";
import { DEFAULT_PAGING } from "@/utils/constants/common";
import { DEFAULT_PRODUCT } from "@/utils/constants/product";
import { parseObjectDescriptionToTreeData } from "@/utils/helpers/schema";
import { IActivityLog } from "@/utils/types/env.type";
import { DownOutlined, UpOutlined } from "@ant-design/icons";
import {
  Button,
  DatePicker,
  Flex,
  Form,
  Input,
  Select,
  Table,
  Tag,
  Tree,
} from "antd";
import { ColumnsType } from "antd/es/table";
import { ExpandableConfig } from "antd/es/table/interface";
import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import ActivityDetailModal from "./components/ActivityDetailModal";
import LogMethodTag from "./components/LogMethodTag";
import styles from "./index.module.scss";

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
  { value: 204, label: "204" },
  { value: 400, label: "400" },
  { value: 401, label: "401" },
  { value: 404, label: "404" },
  { value: 500, label: "500" },
];

const renderExpandIcon: ExpandableConfig<any>["expandIcon"] = ({
  expanded,
  onExpand,
  record,
}) =>
  expanded ? (
    <UpOutlined
      onClick={(e) => onExpand(record, e)}
      style={{ color: "#00000040" }}
    />
  ) : (
    <DownOutlined
      onClick={(e) => onExpand(record, e)}
      style={{ color: "#00000040" }}
    />
  );
const renderExpandRow: ExpandableConfig<any>["expandedRowRender"] = (
  record
) => (
  <Flex>
    <Flex
      vertical
      gap={4}
      className={styles.tree}
      style={{
        padding: "16px 0",
        flex: "0 0 156px",
        borderRight: "1px solid #f0f0f0",
      }}
    >
      <Text.NormalMedium>Parameters</Text.NormalMedium>
      <Tree
        treeData={parseObjectDescriptionToTreeData(
          record.queryParameters,
          styles.treeTitle,
          styles.treeExample
        )}
      />
    </Flex>
    <Flex
      vertical
      gap={4}
      className={styles.tree}
      style={{ flex: 1, padding: "16px 128px 16px 24px" }}
    >
      <Text.NormalMedium>Request:</Text.NormalMedium>
      <Tree
        treeData={parseObjectDescriptionToTreeData(
          record.request,
          styles.treeTitle,
          styles.treeExample
        )}
      />
    </Flex>
    <Flex
      vertical
      gap={4}
      className={styles.tree}
      style={{ flex: 1, padding: "16px 128px 16px 24px" }}
    >
      <Text.NormalMedium>Response:</Text.NormalMedium>
      <Tree
        treeData={parseObjectDescriptionToTreeData(
          record.response,
          styles.treeTitle,
          styles.treeExample
        )}
      />
    </Flex>
  </Flex>
);
const EnvironmentActivityLog = () => {
  const { envId } = useParams();
  const { data: envData } = useGetProductEnvs(DEFAULT_PRODUCT);

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
    DEFAULT_PRODUCT,
    String(envId),
    queryParams
  );

  useEffect(() => {
    if (!isLoading) {
      const updatedTableData = data?.data;
      const updatedPagination = {
        current: data?.page ?? initPagination.current,
        pageSize: data?.size ?? initPagination.pageSize,
        total: 1,
      };
      setPagination(updatedPagination);
      setTableData(updatedTableData!);
    }
  }, [data, isLoading]);
  const handleFormValuesChange = (_: any, values: any) => {
    setQueryParams(values);
  };
  const [form] = Form.useForm();
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
      render: (log: IActivityLog) => (
        <Flex align="center" gap={8}>
          <LogMethodTag method={log.method} />
          <Tag bordered={false}>{log.path}</Tag>
        </Flex>
      ),
    },
    {
      key: "status",
      title: "Status code",
      render: (log: IActivityLog) => log.httpStatusCode,
    },
    {
      key: "date",
      title: "Date",
      render: (log: IActivityLog) => toDateTime(log.createdAt, true),
    },
    {
      key: "action",
      title: "",
      render: (log: IActivityLog) => (
        <Button
          className={styles.rowHoverOnly}
          onClick={() => {
            setModalActivityId(log.requestId);
            setModalOpen(true);
          }}
        >
          View Seller API
        </Button>
      ),
    },
    Table.EXPAND_COLUMN,
  ];
  return (
    <>
      <Flex vertical>
        <Flex
          justify="space-between"
          align="center"
          className={styles.headWrapper}
        >
          <Text.Custom size="20px" bold="700">
            &lt; Development API activity log
          </Text.Custom>
        </Flex>
        <Flex align="center" className={styles.filterWrapper}>
          <Form
            form={form}
            layout="inline"
            colon={false}
            onValuesChange={handleFormValuesChange}
          >
            <Form.Item label="Environment" name="envId">
              <Select options={envOptions} popupMatchSelectWidth={false} />
            </Form.Item>
            <Form.Item label="Time range from" name="requestStartTime">
              <DatePicker placeholder="All" />
            </Form.Item>
            <Form.Item label="to" name="requestEndTime">
              <DatePicker placeholder="All" />
            </Form.Item>
            <Form.Item label="Method" name="method">
              <Select
                options={methodOptions}
                placeholder="All"
                popupMatchSelectWidth={false}
              />
            </Form.Item>
            <Form.Item label="Path" name="path">
              <Input placeholder="All" />
            </Form.Item>
            <Form.Item label="Status code" name="statusCode">
              <Select
                options={statusCodeOptions}
                placeholder="All"
                popupMatchSelectWidth={false}
              />
            </Form.Item>
          </Form>
        </Flex>
        <div className={styles.tableWrapper}>
          <Table
            dataSource={tableData}
            columns={columns}
            rowKey={(record) =>
              `${record.method}_${record.requestId}_${record.createdAt}`
            }
            loading={isLoading}
            className={styles.table}
            rowClassName={styles.hovering}
            expandable={{
              rowExpandable: () => true,
              expandIcon: renderExpandIcon,
              expandedRowRender: renderExpandRow,
              expandedRowClassName: () => styles.expandedRowWrapper,
            }}
            pagination={{
              hideOnSinglePage: true,
              pageSize: pagination.pageSize,
              current: pagination.current + 1,
              onChange: handlePaginationChange,
              total: pagination.total,
              showSizeChanger: true,
              onShowSizeChange: handlePaginationShowSizeChange,
              showTotal: (total) => `Total ${total} items`,
            }}
          />
        </div>
      </Flex>
      <ActivityDetailModal
        envId={String(envId)}
        activityId={modalActivityId ?? ""}
        open={modalOpen}
        setOpen={(value) => setModalOpen(value)}
      />
    </>
  );
};

export default EnvironmentActivityLog;
