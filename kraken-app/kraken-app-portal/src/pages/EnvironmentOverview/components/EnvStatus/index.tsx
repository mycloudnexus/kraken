import { Text } from "@/components/Text";
import { useGetAPIDeploymentStatus, useGetRunningAPIList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IEnv } from "@/utils/types/env.type";
import {
  ApiOutlined,
  CheckCircleFilled,
} from "@ant-design/icons";
import { Flex, Spin, Typography } from "antd";
import classes from "classnames";
import dayjs from "dayjs";
import { upperFirst } from "lodash";
import { useMemo } from "react";
import styles from "./index.module.scss";

interface Props {
  env: IEnv;
  apiKey?: boolean;
  status?: boolean;
  dataPlane?: number;
  disConnect?: number;
  runningApi?: number;
  connect?: number;
  loading?: boolean;
}

const parseColors = (status: string) => {
  switch (status) {
    case "SUCCESS":
      return "#389E0D";
    case "WARNING":
      return "yellow";
    default:
      return "red";
  }
};

const EnvStatus = ({
  env,
  apiKey,
  status,
  dataPlane = 0,
  disConnect = 0,
  connect = 0,
  loading = false,
}: Readonly<Props>) => {
  const { currentProduct } = useAppStore();
  const connectStatus = useMemo(() => {
    if (connect === dataPlane) {
      return "allConnect";
    }
    if (disConnect === dataPlane) {
      return "allDisConnect";
    }
    return "someConnect";
  }, [disConnect, connect, dataPlane]);

  const isDisConnect = useMemo(() => {
    return connectStatus === "allDisConnect";
  }, [connectStatus]);

  const envName = env?.name?.toLocaleLowerCase?.();
  const { data, isLoading: loadingRunningData } = useGetRunningAPIList(
    currentProduct,
    {
      envId: env?.id,
      orderBy: "createdAt",
      direction: "DESC",
      page: 0,
      size: 20,
    }
  );

  const { data: latestDeployment, isLoading } = useGetAPIDeploymentStatus(
    currentProduct,
    env?.id
  );

  return (
    <Flex vertical gap={20} className={styles.container}>
      <Flex gap={20}>
        <Flex
          vertical
          gap={10}
          flex={1}
          className={styles.apiRunning}
          align="center"
          justify="center"
        >
          {loading || loadingRunningData ? (
            <Spin size="small" />
          ) : (
            <Text.BoldMedium>{data?.length || 0}</Text.BoldMedium>
          )}
          <Text.LightMedium>Total running API mapping</Text.LightMedium>
        </Flex>
        <Flex
          vertical
          gap={10}
          flex={1}
          className={classes(styles.statusWrapper, {
            [styles.nothing]: dataPlane === 0,
            [styles.success]: status,
            [styles.error]: isDisConnect,
            [styles.warning]: connectStatus === "someConnect",
            [styles.noAPI]: !apiKey,
          })}
          align="center"
          justify="center"
        >
          <Flex
            align="center"
            gap={8}
            vertical
            flex={1}
            className={styles.dataPlaneInfo}
            style={{ whiteSpace: "nowrap" }}
          >
            {loading ? (
              <Spin size="small" />
            ) : (
              <Text.BoldMedium>{dataPlane}</Text.BoldMedium>
            )}
            <Flex className={styles.dataPlaneInfo} gap={8} align="center">
              <ApiOutlined style={{ width: 14, height: 14 }} />
              <Text.LightMedium>In use data plane</Text.LightMedium>
            </Flex>
          </Flex>
        </Flex>
      </Flex>
      <Flex gap={4} wrap="wrap">
        {latestDeployment?.status && (
          <>
            <CheckCircleFilled
              style={{ color: parseColors(latestDeployment.status) }}
            />
            <Typography.Text style={{ color: parseColors(latestDeployment.status) }}>
              {upperFirst(latestDeployment.status.toLowerCase())}
            </Typography.Text>
          </>
        )}
        <Typography.Text style={{ color: "rgba(0, 0, 0, 0.45)" }}>
          {loading ? (
            <Spin size="small" />
          ) : (
            <Flex gap={4}>
              Last deployed to {upperFirst(envName)}{" "}
              {dayjs
                .utc(latestDeployment?.createAt)
                .local()
                .format("YYYY-MM-DD HH:mm:ss")}
            </Flex>
          )}
        </Typography.Text>
      </Flex>
    </Flex>
  );
};

export default EnvStatus;
