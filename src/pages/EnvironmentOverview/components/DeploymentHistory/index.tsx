import { useGetAPIDeployments } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IEnv } from "@/utils/types/env.type";
import { Flex, Table, Typography } from "antd";
import { useMemo, useState } from "react";
import DeploymentStatus from "../DeploymentStatus";
import styles from "./index.module.scss";
import dayjs from "dayjs";
import RequestMethod from "@/components/Method";
import MappingMatrix from '@/components/MappingMatrix';

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
  const envName = env?.name?.toLocaleLowerCase?.();

  const { data, isLoading } = useGetAPIDeployments(currentProduct, {
    envId: env?.id,
    orderBy: "createdAt",
    direction: "DESC",
    ...pageInfo,
  },
    envName
  );

  const columns = useMemo(
    () => [
      envName === "stage"
        ? {
          title: "API mapping",
          dataIndex: "",
          width: 340,
          render: (item: any) => (
            <Flex gap={10} align="center">
              <RequestMethod method={item?.method} />
              <Typography.Text
                style={{ color: "#2962FF" }}
                ellipsis={{ tooltip: item?.path }}
              >
                {item?.path}
              </Typography.Text>
              <MappingMatrix mappingMatrix={item.mappingMatrix} />
            </Flex>
          ),
        } : {
          title: "Component",
          dataIndex: "",
          width: 340,
          render: (item: any) => (
            <Flex gap={10} align="left" vertical>
              {item.components.map((component: any, index: number) => (
                <Typography.Text
                  key={`${component.componentName}-${index}`}
                  style={{ color: "#2962FF" }} >
                  {component.componentName}
                </Typography.Text>
              ))}
            </Flex>
          ),
        },
      envName === 'production' ? {
        title: "Version",
        dataIndex: "",
        width: 340,
        render: (item: any) => (
          <Flex gap={10} align="left" vertical>
            {item.components.map((component: any, index: number) => (
              <Typography.Text
                key={`${component.version}-${index}`}
              >
                {component.version}
              </Typography.Text>
            ))}
          </Flex>
        ),
      } : {},
      {
        title: "Status",
        dataIndex: "status",
        render: (status: string) => <DeploymentStatus status={status} />,
      },
      {
        title: "Deployed time",
        dataIndex: "createAt",
        width: 340,
        render: (time: string) =>
          dayjs.utc(time).local().format("YYYY-MM-DD HH:mm:ss"),
      },
    ],
    [env]
  );

  const tableData = useMemo(() => {
    if (envName === 'production') {
      return data?.data?.map((item: any) => ({
        name: item.name,
        components: item.components,
        status: item.status,
        createdAt: item.createdAt
      }));
    }
    return data?.data;
  }, [data?.data, envName]);

  return (
    <div>
      <Table
        className={styles.table}
        columns={columns}
        loading={isLoading}
        dataSource={tableData}
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
