import { Flex } from "antd";
import styles from "./index.module.scss";
import classes from "classnames";
import Text from "@/components/Text";
import {
  ApiOutlined,
  CheckCircleFilled,
  InfoCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
} from "@ant-design/icons";
import { useMemo } from "react";

interface Props {
  apiKey?: boolean;
  status?: boolean;
  dataPlane?: number;
  disConnect?: number;
  connect?: number;
}

const EnvStatus = ({
  apiKey,
  status,
  dataPlane = 0,
  disConnect = 0,
  connect = 0,
}: Readonly<Props>) => {
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
  if (!apiKey) {
    return (
      <Flex
        vertical
        gap={6}
        className={classes(styles.statusWrapper, styles.notice)}
      >
        <Text.BoldMedium>Connect to data plane</Text.BoldMedium>
        <Text.LightMedium>
          Click the button below to connect to data plane.
          <br /> The latest deployment will show up here.
        </Text.LightMedium>
      </Flex>
    );
  }
  return (
    <Flex
      vertical
      gap={10}
      className={classes(styles.statusWrapper, {
        [styles.nothing]: dataPlane === 0,
        [styles.success]: status,
        [styles.error]: isDisConnect,
        [styles.warning]: connectStatus === "someConnect",
      })}
      align="flex-start"
    >
      <Flex align="center" gap={8} className={styles.dataPlaneInfo}>
        <ApiOutlined />
        <Text.LightMedium>In use data plane</Text.LightMedium>
        <Text.BoldMedium>{dataPlane}</Text.BoldMedium>
        <InfoCircleOutlined className={styles.dataPlaneInfoIcon} />
      </Flex>
      <Flex align="center" gap={8}>
        {dataPlane == 0 ? (
          <Text.LightSmall style={{ color: "transparent" }}>''</Text.LightSmall>
        ) : (
          <>
            {isDisConnect ? (
              <CloseCircleOutlined />
            ) : connectStatus === "allConnect" ? (
              <CheckCircleFilled />
            ) : (
              <ExclamationCircleOutlined />
            )}
            <Text.LightSmall>
              {`${isDisConnect ? disConnect : connect} / ${dataPlane} `}
              {isDisConnect ? "Disconnected" : "Connected"}
            </Text.LightSmall>
          </>
        )}
      </Flex>
    </Flex>
  );
};

export default EnvStatus;
