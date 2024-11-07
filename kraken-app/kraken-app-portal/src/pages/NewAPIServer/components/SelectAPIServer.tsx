import TitleIcon from "@/assets/title-icon.svg";
import Flex from "@/components/Flex";
import { Text } from "@/components/Text";
import { useGetValidateServerName } from '@/hooks/product';
import { useAppStore } from '@/stores/app.store';
import { isURL } from "@/utils/helpers/url";
import { Form, Input } from "antd";
import { isEmpty } from "lodash";

const SelectAPIServer = () => {
  const { currentProduct } = useAppStore();
  const { mutateAsync: validateName } = useGetValidateServerName();
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
            validator: async (_, name) => {
              const { data: isValid } = await validateName({ productId: currentProduct, name })
              if (isValid) {
                return Promise.resolve();
              } else {
                return Promise.reject(`The name ${name} is already taken`);
              }
             }
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
            validator: (_, value) => {
              if (isURL(value) || isEmpty(value)) {
                return Promise.resolve();
              }
              return Promise.reject(new Error("Please enter a valid URL"));
            },
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
