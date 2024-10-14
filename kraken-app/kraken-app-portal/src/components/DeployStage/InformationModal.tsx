import { CloseCircleOutlined } from "@ant-design/icons";
import { Flex, Modal } from "antd";
import styles from "./index.module.scss";
import Text from "../Text";

type Props = {
  open: boolean;
  onClose?: () => void;
  modalText: string;
};

const InformationModal = ({ open, onClose, modalText }: Props) => {
  return (
    <Modal
      className={styles.modal}
      closable={false}
      open={open}
      okText="Got it"
      cancelButtonProps={{ style: { display: "none" } }}
      onOk={onClose}
    >
      <Flex gap={16} align="flex-start">
        <CloseCircleOutlined style={{ fontSize: 22, color: "#FF4D4F", marginTop: 2 }} />
        <Text.LightLarge>{modalText}</Text.LightLarge>
      </Flex>
    </Modal>
  );
};

export default InformationModal;
