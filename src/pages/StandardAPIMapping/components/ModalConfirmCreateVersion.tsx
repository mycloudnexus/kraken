import Text from "@/components/Text";
import { ExclamationCircleOutlined } from "@ant-design/icons";
import { Modal } from "antd";

export const showModalConfirmCreateVersion = (onOk?: () => any) => {
  return Modal.confirm({
    icon: <ExclamationCircleOutlined />,
    title: "You are going to create a new version",
    content: (
      <Text.NormalMedium>
        Create a new version will keep all your configuration stable and ready
        to deploy. What version you want to create?
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
