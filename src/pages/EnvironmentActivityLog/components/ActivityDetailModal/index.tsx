import LogMethodTag from "@/components/LogMethodTag";
import Text from "@/components/Text";
import { useGetProductEnvActivityDetail } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { parseObjectDescriptionToTreeData } from "@/utils/helpers/schema";
import { IActivityLog } from "@/utils/types/env.type";
import { Collapse, Flex, Modal, Spin, Table, Tag, Tree } from "antd";
import { ColumnsType } from "antd/es/table";
import { useCallback, useMemo } from "react";
import styles from "./index.module.scss";

interface Props {
  envId: string;
  activityId: string;
  open: boolean;
  setOpen: (value: boolean) => void;
}
const ActivityDetailModal = ({
  envId,
  activityId,
  open,
  setOpen,
}: Readonly<Props>) => {
  const { currentProduct } = useAppStore();
  const { data, isLoading } = useGetProductEnvActivityDetail(
    currentProduct,
    envId,
    activityId
  );
  const activityList = useMemo(
    () => (data ? [data.main, ...data.branches] : []),
    [data]
  );
  const collapseItems = useCallback(
    (activity: IActivityLog) => {
      const parameterList = Object.entries(activity.queryParameters).map(
        ([key, value]) => ({
          key,
          type: value,
        })
      );
      const parameterColumns: ColumnsType<any> = [
        {
          key: "name",
          title: "Name",
          render: (item) => `${item.key}`,
        },
        {
          key: "type",
          title: "Type",
          render: (item) => `${item.type}`,
        },
      ];
      return [
        {
          key: "param",
          label: "Parameters",
          children: (
            <Table
              columns={parameterColumns}
              dataSource={parameterList}
              pagination={false}
            />
          ),
        },
        {
          key: "request",
          label: "Request body",
          children: (
            <div className={styles.tree}>
              <Tree
                treeData={parseObjectDescriptionToTreeData(
                  activity.request,
                  styles.treeTitle,
                  styles.treeExample
                )}
              />
            </div>
          ),
        },
        {
          key: "response",
          label: "Response",
          children: (
            <div className={styles.tree}>
              <Tree
                treeData={parseObjectDescriptionToTreeData(
                  activity.response,
                  styles.treeTitle,
                  styles.treeExample
                )}
              />
            </div>
          ),
        },
      ];
    },
    [data]
  );
  const handleOk = () => setOpen(false);
  return (
    <Modal
      title="View downstream API"
      open={open}
      onOk={handleOk}
      cancelButtonProps={{
        style: {
          display: "none",
        },
      }}
      onCancel={handleOk}
      width="auto"
    >
      <Spin spinning={isLoading}>
        <Flex
          gap={33}
          align="stretch"
          style={{ maxHeight: "calc(100vh - 240px)" }}
        >
          {activityList?.map((activity) => (
            <div
              className={styles.activityWrapper}
              key={`${activity.method}_${activity.path}`}
            >
              <Flex vertical gap={24} className={styles.activityHeader}>
                <Text.Custom size="24px">
                  geographicAddressValidation
                </Text.Custom>
                <Flex gap={8} align="center">
                  <LogMethodTag method={activity.method} />
                  <Tag bordered={false}>{activity.path}</Tag>
                  <Text.NormalMedium style={{ color: "#8c8c8c" }}>
                    Creates a GeographicAddressValidation
                  </Text.NormalMedium>
                </Flex>
              </Flex>
              <Collapse
                items={collapseItems(activity)}
                bordered={false}
                defaultActiveKey={["param", "request", "response"]}
                className={styles.collapseWrapper}
              />
            </div>
          ))}
        </Flex>
      </Spin>
    </Modal>
  );
};

export default ActivityDetailModal;
