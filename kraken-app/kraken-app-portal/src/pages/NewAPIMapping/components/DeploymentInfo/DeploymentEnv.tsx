import { SecondaryText } from "@/components/Text";
import useUser from "@/hooks/user/useUser";
import { IApiMapperDeployment } from "@/utils/types/product.type";
import { InfoCircleOutlined } from "@ant-design/icons";
import { Flex, Skeleton, Tooltip } from "antd";
import dayjs from "dayjs";
import { isEmpty } from "lodash";
import styles from "../Deployment/index.module.scss";

export function DeploymentEnv({
  loading,
  deployment,
}: Readonly<{ loading?: boolean; deployment: IApiMapperDeployment }>) {
  const { findUserName } = useUser();

  const renderVersionText = (version: string) => {
    if (isEmpty(version)) {
      return "N/A";
    }
    return `v${version}`;
  };

  const env = deployment?.envName === "stage" ? "Stage" : "Production";

  if (loading) {
    return (
      <Flex gap={4} className={styles.deploymentSkeleton}>
        <Skeleton.Input style={{ height: 16, width: 20 }} active />
        <Skeleton.Input style={{ height: 16, width: 100 }} active />
      </Flex>
    );
  }

  return (
    <Flex gap={8}>
      <SecondaryText.LightSmall lineHeight="20px">
        {renderVersionText(deployment?.runningVersion)}
      </SecondaryText.LightSmall>
      <Flex gap={4}>
        <SecondaryText.LightSmall lineHeight="22px">
          {env}
        </SecondaryText.LightSmall>
        {deployment?.runningVersion && (
          <Tooltip
            title={
              <>
                Running version in {env}
                <br />
                Deployed by {findUserName(deployment?.createBy)}{" "}
                {dayjs(deployment?.createAt).format("YYYY-MM-DD HH:mm:ss")}
              </>
            }
          >
            <InfoCircleOutlined style={{ color: "#00000073" }} />
          </Tooltip>
        )}
      </Flex>
    </Flex>
  );
}
