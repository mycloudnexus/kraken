import { IUser } from "@/utils/types/user.type";
import { Form, Input, Modal, notification } from "antd";
import styles from "./index.module.scss";
import { get } from "lodash";
import { useResetPassword } from "@/hooks/user";

type Props = {
  user: IUser;
  open: boolean;
  onClose: () => void;
};

const ResetPwdModal = ({ user, open, onClose }: Props) => {
  const [form] = Form.useForm();
  const pwd = Form.useWatch("password", form);
  const { mutateAsync: changePwd, isPending: isResetPwd } = useResetPassword();
  const onFinish = async () => {
    try {
      const res = await changePwd({
        id: user.id,
        password: pwd,
      } as any);
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
    <Modal
      open={open}
      title="Reset Password"
      className={styles.root}
      okButtonProps={{
        loading: isResetPwd,
        htmlType: "submit",
      }}
      onOk={form.submit}
      onCancel={onClose}
    >
      <Form
        form={form}
        layout="vertical"
        className={styles.form}
        onFinish={onFinish}
      >
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
        <Form.Item
          required
          name="confirmPassword"
          label="Confirm Password"
          rules={[
            {
              validator: (_, value) => {
                if (pwd === value) {
                  return Promise.resolve();
                }
                return Promise.reject(
                  new Error(
                    "A confirm password has to be the same as the password."
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
    </Modal>
  );
};

export default ResetPwdModal;
