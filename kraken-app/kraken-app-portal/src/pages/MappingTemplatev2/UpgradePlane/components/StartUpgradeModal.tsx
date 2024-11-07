import { Text } from "@/components/Text";
import { Flex, Modal, ModalProps } from "antd";

export function StartUpgradeModal({
  onCancel,
  ...props
}: Readonly<ModalProps>) {
  return (
    <Modal
      {...props}
      width={416}
      okText="Yes, continue"
      centered
      closeIcon={<></>}
      onCancel={onCancel}
    >
      <Flex vertical gap={8}>
        <Text.NormalLarge data-testid="title">
          Are you sure to start upgrade now?
        </Text.NormalLarge>
        <Text.LightMedium data-testid="meta">
          Upgrade may take a while. You will not be able to change the API
          mapping configurations or perform new deployment during the process.
          Continue?
        </Text.LightMedium>
      </Flex>
    </Modal>
  );
}
