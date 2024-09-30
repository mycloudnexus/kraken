import Text from "@/components/Text";
import { CloseOutlined } from "@ant-design/icons";
import {
  Button,
  ConfigProvider,
  Drawer,
  Flex,
  Form,
  Input,
  Select,
  notification,
} from "antd";
import styles from "./index.module.scss";
import { roleOptions } from "../UserRoleEdit";
import Role from "@/components/Role";
import { useCreateUser } from "@/hooks/user";
import { get } from "lodash";

type Props = {
  open: boolean;
  onClose: () => void;
};

export const renderRole = ({ value }: any) => <Role role={value} />;

const UserModal = ({ open, onClose }: Props) => {
  const [form] = Form.useForm();
  const { mutateAsync: createUser, isPending: isCreatePending } =
    useCreateUser();
  const handleFinish = async (values: Record<string, string>) => {
    try {
      const res = await createUser(values as any);
      notification.success({
        message: get(res, "message", "Success!"),
      });
      onClose();
    } catch (error) {
      notification.error({
        message: get(error, "reason", "Please try again."),
      });
    }
  };
  return (
    <Drawer
      width="40vw"
      open={open}
      onClose={onClose}
      title={
        <Flex justify="space-between" align="center">
          <Text.NormalMedium>Create new user</Text.NormalMedium>
          <CloseOutlined onClick={onClose} />
        </Flex>
      }
      footer={
        <Flex gap={12} justify="flex-end" align="center">
          <Button onClick={onClose}>Cancel</Button>
          <Button
            loading={isCreatePending}
            type="primary"
            onClick={form.submit}
          >
            OK
          </Button>
        </Flex>
      }
    >
      <ConfigProvider
        input={{ style: { borderRadius: 2 } }}
        theme={{
          components: {
            Select: {
              borderRadius: 2,
            },
          },
        }}
      >
        <Form
          form={form}
          layout="vertical"
          className={styles.root}
          onFinish={handleFinish}
        >
          <Form.Item
            name="name"
            label="User name"
            rules={[{ required: true, message: "Please enter user name" }]}
          >
            <Input
              placeholder="Please enter"
              autoComplete="off"
              autoCorrect="off"
            />
          </Form.Item>
          <Form.Item
            name="email"
            label="Email address"
            rules={[{ required: true, message: "Please enter email" }]}
          >
            <Input placeholder="Please enter" />
          </Form.Item>
          <Form.Item
            name="role"
            label="User role"
            rules={[{ required: true, message: "Please select user role" }]}
          >
            <Select
              placeholder="Please enter"
              options={roleOptions}
              labelRender={renderRole}
            />
          </Form.Item>
          <Form.Item
            required
            name="password"
            label="Password"
            rules={[
              {
                validator: (_, value) => {
                  if (
                    /^(?=.*[A-Z])(?=.*\d)(?=.*[\W_])(?=.{12,})(?!.*\s).+$/.test(
                      value
                    )
                  ) {
                    return Promise.resolve();
                  }
                  return Promise.reject(
                    new Error(
                      "You must be a password that is at least 12 characters long, 1 uppercase letter, 1 symbol, 1 number, and no spaces."
                    )
                  );
                },
              },
            ]}
          >
            <Input.Password
              placeholder="Please enter"
              autoCorrect="off"
              autoComplete="new-password"
            />
          </Form.Item>
        </Form>
      </ConfigProvider>
    </Drawer>
  );
};

export default UserModal;
