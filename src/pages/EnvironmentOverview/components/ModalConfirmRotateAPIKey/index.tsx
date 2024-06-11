import Text from "@/components/Text";
import { ExclamationCircleOutlined } from "@ant-design/icons";
import { Modal } from "antd";

export const showModalConfirmRotate = (envName: string, onOk?: () => any) => {
  return Modal.confirm({
    icon: <ExclamationCircleOutlined />,
    title: `Rotate the API key for environment "${envName}"?`,
    content: (
      <Text.NormalMedium>
        This will revoke the old API key and issue a new one.
        <br />
        If there are any data plane in your code that are still using the old
        API key, they will immediately be disconnected.
        <br />
        Are you sure you want to proceed? This cannot be undone.
      </Text.NormalMedium>
    ),
    okButtonProps: {
      type: "primary",
    },
    okText: "Rotate",
    okType: "danger",
    onOk,
  });
};
