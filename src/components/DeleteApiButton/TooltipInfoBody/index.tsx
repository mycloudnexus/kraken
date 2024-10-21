import { Text } from "@/components/Text";
import { ExclamationCircleFilled } from "@ant-design/icons";
import { Flex, Button } from "antd";

const TooltipInfoBody = (
  setOpenDrawer: (value: boolean) => void,
  setOpenTooltip: (value: boolean) => void
) => (
  <Flex vertical gap={20}>
    <Flex gap={12}>
      <Flex>
        <ExclamationCircleFilled style={{ color: "#FAAD14" }} />
      </Flex>
      <Flex vertical>
        <Text.NormalLarge color="black">
          This API server can't be deleted
        </Text.NormalLarge>
        <Text.LightMedium color="rgba(0,0,0,0.45)">
          Some endpoints in this API server are being used.
        </Text.LightMedium>
      </Flex>
    </Flex>
    <Flex justify="end" gap={12}>
      <Button
        type="link"
        onClick={() => {
          setOpenDrawer(true);
          setOpenTooltip(false);
        }}
      >
        Check details
      </Button>
    </Flex>
  </Flex>
);

export default TooltipInfoBody;
