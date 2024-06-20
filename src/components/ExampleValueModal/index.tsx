import { Input, Modal, Tag } from "antd";
import Flex from "../Flex";
import styles from "./index.module.scss";
import Text from "../Text";
const { TextArea } = Input;
type Props = {
  method: string;
  attribute: string;
  isOpen: boolean;
  onClose: () => void;
  onOK: (value: string) => void;
};

const ExampleValueModal = ({
  isOpen,
  method,
  attribute,
  onClose,
  onOK,
}: Props) => {
  const handleOK = () => {
    onOK("");
  };
  return (
    <Modal
      open={isOpen}
      onCancel={onClose}
      onOk={handleOK}
      title="Add example value with variable"
      className={styles.modal}
    >
      <Flex justifyContent="flex-start" gap={8} style={{ marginBottom: 4 }}>
        <Tag color="blue">{method}</Tag>
        <Text.LightMedium>{attribute}</Text.LightMedium>
      </Flex>
      <TextArea rows={2} />
    </Modal>
  );
};

export default ExampleValueModal;
