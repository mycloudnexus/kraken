import { useGetAPIDeployments } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IEnv } from "@/utils/types/env.type";
import { Flex, Table, Typography } from "antd";
import { useCallback, useMemo, useState } from "react";
import DeploymentStatus from "../DeploymentStatus";
import styles from "./index.module.scss";
import dayjs from "dayjs";
import RequestMethod from "@/components/Method";

type Props = {
  env?: IEnv;
};

const defaultPage = {
  size: 5,
  page: 0,
};

const DeploymentHistory = ({ env }: Props) => {
  const [pageInfo, setPageInfo] = useState(defaultPage);
  const { currentProduct } = useAppStore();
  const { data, isLoading } = useGetAPIDeployments(currentProduct, {
    envId: env?.id,
    orderBy: "createdAt",
    direction: "DESC",
    ...pageInfo,
  });

  const renderTextType = useCallback((type: string) => {
    switch (type) {
      case "uni":
        return "UNI";
      case "access_e_line":
        return "Access E-line";
      default:
        return type;
    }
  }, []);

  const columns = useMemo(
    () => [
      {
        title:
          env?.name?.toLowerCase() === "stage" ? "API mapping" : "Component",
        dataIndex: "",
        width: 400,
        render: (item: any) => (
          <Flex gap={10} align="center">
            {!!item.medthod && <RequestMethod method={item?.method} />}
            <Typography.Text
              style={{ color: "#2962FF" }}
              ellipsis={{ tooltip: item?.path }}
            >
              {item?.path}
            </Typography.Text>
            <Flex align="center" gap={8}>
              <div className={styles.tagInfo}>
                {renderTextType(item.productType)}
              </div>
              {item.actionType ? (
                <div
                  className={styles.tagInfo}
                  style={{ textTransform: "capitalize" }}
                >
                  {item.actionType}
                </div>
              ) : null}
            </Flex>
          </Flex>
        ),
      },
      {
        title: "Status",
        dataIndex: "status",
        render: (status: string) => <DeploymentStatus status={status} />,
      },
      {
        title: "Deployed time",
        dataIndex: "createAt",
        render: (time: string) =>
          dayjs.utc(time).local().format("YYYY-MM-DD HH:mm:ss"),
      },
    ],
    [env, renderTextType]
  );
  return (
    <div>
      <Table
        className={styles.table}
        columns={columns}
        loading={isLoading}
        dataSource={data?.data}
        pagination={{
          current: data?.page + 1,
          pageSize: data?.size,
          total: data?.total,
          onChange(page, pageSize) {
            setPageInfo({ size: pageSize, page: page - 1 });
          },
          size: "small",
        }}
        rowKey={(item) => JSON.stringify(item)}
      />
    </div>
  );
};

export default DeploymentHistory;
