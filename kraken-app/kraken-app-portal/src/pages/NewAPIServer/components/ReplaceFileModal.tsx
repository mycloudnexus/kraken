import Flex from "@/components/Flex";
import { Text } from "@/components/Text";
import { ExclamationCircleOutlined } from "@ant-design/icons";
import { Modal } from "antd";

type Props = { isOpen: boolean; onOk: () => void; onCancel: () => void };

const ReplaceFileModal = ({ isOpen, onOk, onCancel }: Props) => {
  return (
    <Modal
      open={isOpen}
      onOk={onOk}
      closable={false}
      okText="Continue"
      okButtonProps={{
        style: {
          background: "#FF4D4F",
        },
      }}
      onCancel={onCancel}
    >
      <Flex
        justifyContent="flex-start"
        gap={16}
        style={{ marginBottom: 24, marginTop: 16 }}
        alignItems="flex-start"
      >
        <ExclamationCircleOutlined style={{ color: "#FAAD14", marginTop: 5 }} />
        <Flex flexDirection="column" alignItems="flex-start" gap={8}>
          <Text.NormalLarge>
            Upload a new file will replace the existing one
          </Text.NormalLarge>
          <Text.LightMedium>
            You are going to upload a new API spec to replace the existing one.
          </Text.LightMedium>
        </Flex>
      </Flex>
    </Modal>
  );
};

export default ReplaceFileModal;
