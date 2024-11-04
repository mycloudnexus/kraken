import { Text } from "@/components/Text";
import { CloseCircleOutlined } from "@ant-design/icons";
import { Flex, Modal, ModalProps } from "antd";
import styles from "../index.module.scss";

export function IncompatibleMappingModal({
  onCancel,
  ...props
}: Readonly<ModalProps>) {
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
            Kraken version running in stage data plane is not compatible with
            this mapping template
          </Text.NormalLarge>
          <Text.LightMedium data-testid="meta">
            Please ensure to upgrade kraken in stage data plane to a compatible
            version first and test all the running use cases before moving to
            production data plane upgrade.
          </Text.LightMedium>
        </Flex>
      </Flex>
    </Modal>
  );
}
