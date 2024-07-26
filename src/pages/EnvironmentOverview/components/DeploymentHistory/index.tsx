import { useGetAPIDeployments } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IEnv } from "@/utils/types/env.type";
import { Flex, Table, Typography } from "antd";
import { useMemo, useState } from "react";
import DeploymentStatus from "../DeploymentStatus";
import styles from "./index.module.scss";
import dayjs from "dayjs";
import RequestMethod from "@/components/Method";
import ProductActionType from "@/components/ProductActionType";

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

  const columns = useMemo(
    () => [
      {
        title:
          env?.name?.toLowerCase() === "stage" ? "API mapping" : "Component",
        dataIndex: "",
        width: "50%",
        render: (item: any) => (
          <Flex gap={10} align="center">
            <RequestMethod method={item?.method} />
            <Typography.Text
              style={{ color: "#2962FF" }}
              ellipsis={{ tooltip: item?.path }}
            >
              {item?.path}
            </Typography.Text>
            <ProductActionType
              actionType={item?.actionType}
              productType={item?.productType}
            />
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
    [env]
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
