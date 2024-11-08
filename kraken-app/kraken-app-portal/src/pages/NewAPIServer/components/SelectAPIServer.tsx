import TitleIcon from "@/assets/title-icon.svg";
import Flex from "@/components/Flex";
import { Text } from "@/components/Text";
import { useGetValidateServerName } from '@/hooks/product';
import { useAppStore } from '@/stores/app.store';
import { validateServerName, validateURL } from '@/utils/helpers/validators';
import { Form, FormInstance, Input } from "antd";
import { useMemo } from 'react';

type Props = {
  form?: FormInstance<any>;
};

const SelectAPIServer = ({ form }: Props) => {
  const { currentProduct } = useAppStore();
  const { mutateAsync: validateName } = useGetValidateServerName();
  const originalName = useMemo(() => (form?.getFieldsValue(["name"]).name ?? null), [form]);
  return (
    <>
      <Flex gap={8} justifyContent="flex-start">
        <TitleIcon />
        <Text.NormalLarge>Seller API Server basics</Text.NormalLarge>
      </Flex>
      <Form.Item
        data-testid="api-seller-name-container"
        label="Seller API Server Name"
        name="name"
        rules={[
          {
            required: true,
            message: "Please complete this field.",
          },
          {
            validator: (_, name) => validateServerName(validateName, currentProduct, name, originalName)
          }
        ]}
        validateDebounce={1000}
        labelCol={{ span: 24 }}
      >
        <Input data-testid="api-seller-name-input" placeholder="Add API Server Name" style={{ width: "100%" }} />
      </Form.Item>
      <Form.Item label="Description" name="description" labelCol={{ span: 24 }}>
        <Input placeholder="Add description" style={{ width: "100%" }} />
      </Form.Item>
      <Form.Item
        label="Online API document link"
        name="link"
        rules={[
          {
            required: false,
          },
          {
            validator: validateURL
          },
        ]}
        labelCol={{ span: 24 }}
        validateTrigger="onBlur"
      >
        <Input placeholder="Add link" style={{ width: "100%" }} />
      </Form.Item>
    </>
  );
};

export default SelectAPIServer;
