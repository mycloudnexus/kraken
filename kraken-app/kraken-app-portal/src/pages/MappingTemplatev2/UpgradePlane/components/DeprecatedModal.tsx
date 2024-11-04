import { Text } from "@/components/Text";
import { CloseCircleOutlined } from "@ant-design/icons";
import { Flex, Modal, ModalProps } from "antd";
import styles from "../index.module.scss";

export function DeprecatedModal({ onCancel, ...props }: Readonly<ModalProps>) {
  return (
    <Modal
      {...props}
      okText="Got it"
      width={416}
      cancelButtonProps={{ style: { display: "none" } }}
      centered
      onCancel={onCancel}
    >
      <Flex gap={16} align="flex-start">
        <CloseCircleOutlined className={styles.errorIcon} />
        <Flex vertical gap={8}>
          <Text.NormalLarge data-testid="title">
            This mapping template is depreacted
          </Text.NormalLarge>
          <Text.LightMedium data-testid="meta">
            A newer version mapping template started to upgarde.
          </Text.LightMedium>
        </Flex>
      </Flex>
    </Modal>
  );
}
