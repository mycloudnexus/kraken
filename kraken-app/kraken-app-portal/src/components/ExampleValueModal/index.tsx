import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { Input, Modal, Tag } from "antd";
import { get } from "lodash";
import { useEffect, useState } from "react";
import Flex from "../Flex";
import { Text } from "../Text";
import styles from "./index.module.scss";

const { TextArea } = Input;
type Props = {
  attribute: string;
  isOpen: boolean;
  onClose: () => void;
  onOK: (value: string) => void;
  location: string;
  defaultValue?: string;
};

const ExampleValueModal = ({
  isOpen,
  attribute,
  onClose,
  onOK,
  location,
  defaultValue,
}: Props) => {
  const [value, setValue] = useState("");
  const { sellerAPIExampleProps } = useNewApiMappingStore();
  const handleOK = () => {
    onOK(value);
  };
  useEffect(() => {
    const storeItem = get(
      sellerAPIExampleProps,
      `${location === "PATH" ? "path" : "param"}.${attribute}`,
      ""
    );
    /*if (storeItem) {
      setValue(storeItem);
    }*/
    console.log('storeItem:', storeItem);
    const initial = defaultValue || '';
    setValue(initial);
  }, [sellerAPIExampleProps, defaultValue, attribute, location]);
  return (
    <Modal
      open={isOpen}
      onCancel={onClose}
      onOk={handleOK}
      title="Add example value with variable"
      className={styles.modal}
      okButtonProps={{ "data-testid": "example-btn" }}
      maskClosable={false}
    >
      <Flex justifyContent="flex-start" style={{ marginBottom: 4 }}>
        <Tag color="blue">JSON</Tag>
        <Text.LightMedium>{attribute}</Text.LightMedium>
      </Flex>
      <TextArea
        data-testid="example-text-area"
        rows={2}
        value={value}
        onChange={(e) => setValue(e.target.value)}
      />
    </Modal>
  );
};

export default ExampleValueModal;
