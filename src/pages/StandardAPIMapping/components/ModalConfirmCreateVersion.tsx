import Text from "@/components/Text";
import { Modal } from "antd";

export const showModalConfirmCreateVersion = ({
  onOk,
  className,
}: {
  onOk?: () => any;
  className?: string;
}) => {
  return Modal.confirm({
    icon: <></>,
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
    okText: "OK",
    onOk,
    className,
  });
};
