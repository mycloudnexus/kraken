import Flex from "@/components/Flex";
import Text from "@/components/Text";
import useUser from "@/hooks/user/useUser";
import { InfoCircleOutlined } from "@ant-design/icons";
import { Tooltip } from "antd";
import dayjs from "dayjs";
import { isEmpty } from "lodash";
import { useCallback, useMemo } from "react";
import styles from "../../index.module.scss";

type Props = {
  runningData: any[];
};

const DeploymentInfo = ({ runningData }: Props) => {
  const { findUserName } = useUser();

  const latestRunning = useMemo(() => {
    const stage = runningData?.find(
      (item: any) => item?.envName?.toLowerCase?.() === "stage"
    );
    const production = runningData?.find(
      (item: any) => item?.envName?.toLowerCase?.() === "production"
    );
    return { stage, production };
  }, [runningData]);

  const renderVersionText = useCallback((version: string) => {
    if (isEmpty(version)) {
      return "n/a";
    }
    return `v${version}`;
  }, []);

  return (
    <Flex justifyContent="flex-end" className={styles.versionWrapper} gap={16}>
      <Flex gap={8}>
        <Text.LightSmall lineHeight="20px" color="#389E0D">
          {renderVersionText(latestRunning?.stage?.runningVersion)}
        </Text.LightSmall>
        <Flex gap={4}>
          <Text.LightMedium lineHeight="22px" color="#00000073">
            Stage
          </Text.LightMedium>
          {latestRunning?.stage?.runningVersion ? (
            <Tooltip
              title={
                <>
                  Running version in Stage
                  <br />
                  {latestRunning?.stage?.runningVersion ? (
                    <>
                      Deployed by {findUserName(latestRunning?.stage?.createBy)}{" "}
                      {dayjs(latestRunning?.stage?.createAt).format(
                        "YYYY-MM-DD HH:mm:ss"
                      )}
                    </>
                  ) : (
                    ""
                  )}
                </>
              }
            >
              <InfoCircleOutlined style={{ color: "#00000073" }} />
            </Tooltip>
          ) : (
            <></>
          )}
        </Flex>
      </Flex>
      <Flex gap={8}>
        <Text.LightSmall lineHeight="20px" color="#389E0D">
          {renderVersionText(latestRunning?.production?.runningVersion)}
        </Text.LightSmall>
        <Flex gap={4}>
          <Text.LightMedium lineHeight="22px" color="#00000073">
            Production
          </Text.LightMedium>
          {latestRunning?.production?.runningVersion ? (
            <Tooltip
              title={
                <>
                  Running version in Production
                  <br />
                  Deployed by{" "}
                  {findUserName(latestRunning?.production?.createBy)}{" "}
                  {dayjs(latestRunning?.production?.createAt).format(
                    "YYYY-MM-DD HH:mm:ss"
                  )}
                </>
              }
            >
              <InfoCircleOutlined style={{ color: "#00000073" }} />
            </Tooltip>
          ) : (
            <></>
          )}
        </Flex>
      </Flex>
    </Flex>
  );
};

export default DeploymentInfo;
