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
        Creating a Version can be regarded a snapshot of all the mappings under
        this API component.
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
