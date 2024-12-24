import { useGetProductEnvActivityDetail } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IActivityLog } from "@/utils/types/env.type";
import { Flex, Button, Spin, Drawer } from "antd";
import { ColumnsType } from "antd/es/table";
import { useCallback, useMemo } from "react";
import ActivityDetailItem from "./ActivityDetailItem";
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
          width: 160,
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
      maskClosable
      width={"80%"}
      footer={
        <div style={{ textAlign: "right" }}>
          <Button type="primary" onClick={handleOk}>
            OK
          </Button>
        </div>
      }
    >
      <Flex className={styles.details}>
        <Spin spinning={isLoading}>
          {activityList?.map((activity, n) => (
            <ActivityDetailItem
              key={`${activity.path}-${n}`}
              activity={activity}
              title={n === 0 ? "Sonata API" : "Seller API"}
              collapseItems={collapseItems}
            />
          ))}
        </Spin>
      </Flex>
    </Drawer>
  );
};

export default ActivityDetailModal;
