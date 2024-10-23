import TitleIcon from "@/assets/title-icon.svg";
import Flex from "@/components/Flex";
import { Text } from "@/components/Text";
import { isURL } from "@/utils/helpers/url";
import { Form, Input } from "antd";
import { isEmpty } from "lodash";

const SelectAPIServer = () => {
  return (
    <>
      <Flex gap={8} justifyContent="flex-start">
        <TitleIcon />
        <Text.NormalLarge>Seller API Server basics</Text.NormalLarge>
      </Flex>
      <Form.Item
        label="Seller API Server Name"
        name="name"
        rules={[
          {
            required: true,
            message: "Please complete this field.",
          },
        ]}
        labelCol={{ span: 24 }}
      >
        <Input placeholder="Add API Server Name" style={{ width: "100%" }} />
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
