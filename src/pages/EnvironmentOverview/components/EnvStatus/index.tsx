import { Flex } from "antd";
import styles from "./index.module.scss";
import classes from "classnames";
import Text from "@/components/Text";
import { ApiOutlined, InfoCircleOutlined } from "@ant-design/icons";
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

  return (
    <Flex
      vertical
      gap={10}
      className={classes(styles.statusWrapper, {
        [styles.nothing]: dataPlane === 0,
        [styles.success]: status,
        [styles.error]: isDisConnect,
        [styles.warning]: connectStatus === "someConnect",
        [styles.noAPI]: !apiKey,
      })}
      align="flex-start"
    >
      <Flex
        align="center"
        gap={8}
        className={styles.dataPlaneInfo}
        style={{ whiteSpace: "nowrap" }}
      >
        <ApiOutlined style={{ width: 14, height: 14 }} />
        <Text.LightMedium>In use data plane</Text.LightMedium>
        <Text.BoldMedium>{dataPlane}</Text.BoldMedium>
        <InfoCircleOutlined
          className={styles.dataPlaneInfoIcon}
          style={{ width: 14, height: 14 }}
        />
      </Flex>
    </Flex>
  );
};

export default EnvStatus;
