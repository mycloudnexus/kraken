import { Flex } from "antd";
import styles from "./index.module.scss";
import classes from "classnames";
import Text from "@/components/Text";
import {
  ApiOutlined,
  CheckCircleFilled,
  InfoCircleOutlined,
} from "@ant-design/icons";

interface Props {
  apiKey?: boolean;
  status?: boolean;
  dataPlane?: number;
}

const EnvStatus = ({ apiKey, status, dataPlane }: Readonly<Props>) => {
  if (!apiKey) {
    return (
      <Flex
        vertical
        gap={6}
        className={classes(styles.statusWrapper, styles.notice)}
      >
        <Text.BoldMedium>Connect to data plane</Text.BoldMedium>
        <Text.NormalMedium>
          Click the button below to connect to data plane.
          <br /> The latest deployment will show up here.
        </Text.NormalMedium>
      </Flex>
    );
  }
  return (
    <Flex
      vertical
      gap={10}
      className={classes(styles.statusWrapper, {
        [styles.success]: status,
        [styles.error]: !status,
      })}
      align="flex-start"
    >
      <Flex align="center" gap={8}>
        <CheckCircleFilled />
        <Text.NormalMedium>
          {status ? "Connected" : "Disconnected"}
        </Text.NormalMedium>
      </Flex>
      <Flex align="center" gap={8} className={styles.dataPlaneInfo}>
        <ApiOutlined />
        <Text.NormalMedium>In use data plane</Text.NormalMedium>
        <Text.BoldMedium>{dataPlane}</Text.BoldMedium>
        <InfoCircleOutlined className={styles.dataPlaneInfoIcon} />
      </Flex>
    </Flex>
  );
};

export default EnvStatus;
