import { ExclamationCircleOutlined } from '@ant-design/icons';
import { Modal, Flex, Button } from 'antd';
import Text from "@/components/Text";

export const showModalChangePath = (handleSave: (isExit: boolean) => void, handleRevert: () => void) => {
  Modal.confirm({
    width: 416,
    icon: <ExclamationCircleOutlined />,
    title:
      "You have unsaved content. You are going to switch to another page",
    content: (
      <Text.LightMedium>
        Select save will save the mappings before switching to another page.
        <br />
        Select drop will not save the mappings and switch.
      </Text.LightMedium>
    ),
    footer: (_, { CancelBtn, OkBtn }) => (
      <Flex gap={8} justify="flex-end">
        <CancelBtn />
        <Button
          type="default"
          onClick={() => {
            Modal.destroyAll();
            handleRevert();
          }}
        >
          Drop
        </Button>
        <OkBtn />
      </Flex>
    ),
    okButtonProps: {
      type: "primary",
    },
    okText: "Save",
    onOk: async () => {
      Modal.destroyAll();
      handleSave(true);
    },
    cancelButtonProps: {
      type: "text",
      style: {
        color: "#1890FF",
      },
    },
  });
};
