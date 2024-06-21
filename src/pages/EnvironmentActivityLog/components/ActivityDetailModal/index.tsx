import LogMethodTag from "@/components/LogMethodTag";

import { useGetProductEnvActivityDetail } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { parseObjectDescriptionToTreeData } from "@/utils/helpers/schema";
import { IActivityLog } from "@/utils/types/env.type";
import { Flex, Button, Spin, Table, Tree, Drawer, Typography } from "antd";
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
    () => (data ? [data.main, ...(data.branches ?? [])] : []),
    [data]
  );
  const collapseItems = useCallback(
    (activity: IActivityLog): any => {
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
      return { parameterList, parameterColumns };
    },
    [data]
  );

  const handleOk = () => setOpen(false);
  return (
    <Drawer
      title="View Details"
      onClose={handleOk}
      open={open}
      width={"80%"}
      footer={
        <div style={{ textAlign: "right" }}>
          <Button type="primary" onClick={handleOk}>
            OK
          </Button>
        </div>
      }
    >
      <Spin spinning={isLoading}>
        <Flex
          gap={18}
          align="stretch"
          style={{ maxHeight: "calc(100vh - 108px)" }}
        >
          {activityList?.map((activity, n) => (
            <div className={styles.activity} key={activity.requestId}>
              <h1>{n === 0 ? "Sonota API" : "Seller API"}</h1>
              <div
                className={styles.activityWrapper}
                key={`${activity.method}_${activity.path}`}
              >
                <Flex vertical gap={24} className={styles.activityHeader}>
                  <Flex gap={8} align="center">
                    <LogMethodTag method={activity.method} />
                    <Typography.Text ellipsis={{ tooltip: true }}>
                      {activity.path}
                    </Typography.Text>
                  </Flex>
                </Flex>
                <div className={styles.activityBody}>
                  <h3>Parameters</h3>
                  <Table
                    columns={collapseItems(activity)?.parameterColumns}
                    dataSource={collapseItems(activity)?.parameterList}
                    pagination={false}
                  />
                  <h3>Request body</h3>
                  <div className={styles.tree}>
                    <Tree
                      treeData={parseObjectDescriptionToTreeData(
                        activity.request,
                        styles.treeTitle,
                        styles.treeExample
                      )}
                    />
                  </div>
                  <h3>Responses</h3>
                  <div className={styles.tree}>
                    <Tree
                      treeData={parseObjectDescriptionToTreeData(
                        activity.response,
                        styles.treeTitle,
                        styles.treeExample
                      )}
                    />
                  </div>
                </div>
              </div>
            </div>
          ))}
        </Flex>
      </Spin>
    </Drawer>
  );
};

export default ActivityDetailModal;
