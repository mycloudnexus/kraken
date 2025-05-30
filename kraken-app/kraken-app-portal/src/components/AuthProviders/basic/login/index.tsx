import loginBg from "@/assets/login/login-bg.svg?url";
import Logo from "@/assets/logo.svg";
import Flex from "@/components/Flex";
import { useLogin } from "@/hooks/login";
import { PRODUCT_NAME } from "@/utils/constants/common";
import { Alert, Button, Form, Input, Typography } from "antd";
import { useState } from "react";
import styles from "./index.module.scss";
import { useBasicAuth } from "@/components/AuthProviders/basic/provider/BasicAuthProvider";

const Login = () => {
  const { isPending } = useLogin();
  const { loginWithCredentials } = useBasicAuth();

  const [error] = useState<string | null>(null);

  const onFinish = async (values: any) : Promise<void> => {
    return await loginWithCredentials(values);
  }

  return (
    <Flex
      className={styles.pageWrapper}
      style={{ backgroundImage: `url(${loginBg})` }}
    >
      <Flex flexDirection="column" gap={24} className={styles.formWrapper}>
        <Flex gap={12} className={styles.logoWrapper}>
          <Logo />
          <Typography.Text
            style={{
              fontSize: 20,
              fontWeight: "bold",
              fontFamily: "Montserrat, sans-serif",
            }}
          >
            {PRODUCT_NAME}
          </Typography.Text>
        </Flex>
        <Flex flexDirection="column">
          <Typography.Text style={{ fontSize: 18, fontWeight: 600 }}>
            Welcome
          </Typography.Text>
          <Typography.Text>
            Enter your Account Credential to get start
          </Typography.Text>
        </Flex>

        {error && (
          <Alert className={styles.errorAlert} type="error" message={error} />
        )}

        <Form layout="vertical" className={styles.form} onFinish={onFinish}>
          <Form.Item name="userName">
            <Input placeholder="User Name" required />
          </Form.Item>
          <Form.Item name="password">
            <Input.Password placeholder="Password" required />
          </Form.Item>
          <Button
            data-testid="btn-login"
            type="primary"
            htmlType="submit"
            loading={isPending}
            block
          >
            Login
          </Button>
        </Form>
      </Flex>
    </Flex>
  );
};

export default Login;
