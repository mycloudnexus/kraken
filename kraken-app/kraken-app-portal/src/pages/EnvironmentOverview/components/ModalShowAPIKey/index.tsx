import { Text } from "@/components/Text";
import { ExclamationCircleOutlined } from "@ant-design/icons";
import { Button, Flex, Input, Modal } from "antd";

export const showModalShowNew = (apiKey: string) => {
  const copyKeyToClipboard = () => {
    navigator.clipboard.writeText(apiKey);
  };
  return Modal.confirm({
    icon: <ExclamationCircleOutlined />,
    title: "Here's your API key",
    content: (
      <>
        <Text.NormalMedium>
          We only show this to you once (right now). If you forget it, you will
          need to generate a new one.
        </Text.NormalMedium>
        <Flex
          justify="space-between"
          align="center"
          gap={24}
          style={{ marginTop: 16 }}
        >
          <Input variant="borderless" value={apiKey} style={{ padding: 0 }} />
          <Button onClick={copyKeyToClipboard}>Copy</Button>
        </Flex>
      </>
    ),
    footer: (_, { CancelBtn }) => <CancelBtn />,
    cancelButtonProps: {
      type: "primary",
      danger: true,
    },
    cancelText: "Done",
  });
};
