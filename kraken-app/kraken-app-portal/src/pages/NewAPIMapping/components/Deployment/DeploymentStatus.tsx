import { Text } from "@/components/Text";
import useUser from "@/hooks/user/useUser";
import { IApiMapperDeployment } from "@/utils/types/product.type";
import { InfoCircleOutlined } from "@ant-design/icons";
import { Flex, Skeleton, Tooltip } from "antd";
import dayjs from "dayjs";
import { renderDeployText } from "../../helper";
import StatusIcon from "../StatusIcon";
import styles from "./index.module.scss";

export function DeploymentStatus({
  loading,
  deployment,
}: Readonly<{ loading?: boolean; deployment?: IApiMapperDeployment }>) {
  const { findUserName } = useUser();

  if (loading) {
    return (
      <Flex gap={4} className={styles.deploymentSkeleton}>
        <Skeleton.Avatar size={16} active />
        <Skeleton.Input style={{ height: 16, width: 100 }} active />
      </Flex>
    );
  }

  return (
    <Flex gap={4}>
      <StatusIcon status={deployment?.status as any} />
      <Text.LightSmall
        data-testid="deploymentEnv"
        lineHeight="20px"
        style={{ textTransform: "capitalize" }}
      >
        {deployment?.envName}
      </Text.LightSmall>
      <Tooltip
        title={
          <>
            Deploy {renderDeployText(deployment?.status as any)}
            <br />
            <>
              By {findUserName(deployment?.createBy as any)}{" "}
              {dayjs(deployment?.createAt).format("YYYY-MM-DD HH:mm:ss")}
            </>
          </>
        }
      >
        <InfoCircleOutlined />
      </Tooltip>
    </Flex>
  );
}
