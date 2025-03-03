import loginBg from "@/assets/login/login-bg.svg?url";
import Logo from "@/assets/logo.svg";
import Flex from "@/components/Flex";
import { useLogin } from "@/hooks/login";
import { PRODUCT_NAME } from "@/utils/constants/common";
import { storeData } from "@/utils/helpers/token";
import { Alert, Button, Form, Input, Typography } from "antd";
import { get } from "lodash";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./index.module.scss";

const Login = () => {
  const navigate = useNavigate();
  const { mutateAsync: login, isPending } = useLogin();

  const [error, setError] = useState<string | null>(null);

  const handleFinish = async (values: any) => {
    try {
      const res = await login(values);
      const accessToken = get(res, "data.accessToken");
      const expiresIn = get(res, "data.expiresIn");
      const refreshToken = get(res, "data.refreshToken");
      const refreshTokenExpiresIn = get(res, "data.refreshTokenExpiresIn");

      if (accessToken && expiresIn) {
        storeData("token", accessToken);
        storeData("tokenExpired", String(Date.now() + expiresIn * 1000));
        storeData("refreshToken", refreshToken);
        storeData(
          "refreshTokenExpiresIn",
          String(Date.now() + refreshTokenExpiresIn * 1000)
        );
        setError(null);
        navigate("/", { replace: true });
      } else {
        throw new Error("Invalid username or password.");
      }
    } catch (e: any) {
      setError(e.message || "Error on login!");
    }
  };

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

        <Form layout="vertical" className={styles.form} onFinish={handleFinish}>
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
