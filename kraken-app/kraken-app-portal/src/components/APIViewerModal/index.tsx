import { Button, Drawer } from "antd";
import { Text } from "../Text";
import styles from "./index.module.scss";
import Flex from "../Flex";
import { CloseOutlined } from "@ant-design/icons";
import APIViewerContent from "../APIViewerContent";

type Props = {
  content: string;
  isOpen: boolean;
  onClose: () => void;
  selectedAPI?: string;
};

const APIViewerModal = ({ selectedAPI, content, isOpen, onClose }: Props) => {
  return (
    <Drawer
      className={styles.modal}
      open={isOpen}
      maskClosable
      title={
        <Flex justifyContent="space-between">
          <Text.BoldLarge>API details</Text.BoldLarge>
          <CloseOutlined
            style={{ color: "#00000073" }}
            onClick={onClose}
            role="none"
          />
        </Flex>
      }
      closable={false}
      onClose={onClose}
      width="70vw"
      footer={
        <Flex justifyContent="flex-end">
          <Button type="primary" onClick={onClose}>
            OK
          </Button>
        </Flex>
      }
    >
      <APIViewerContent selectedAPI={selectedAPI} content={content} />
    </Drawer>
  );
};

export default APIViewerModal;
