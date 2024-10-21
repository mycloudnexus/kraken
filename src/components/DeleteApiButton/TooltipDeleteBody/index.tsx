import { Text } from "@/components/Text";
import { ExclamationCircleFilled } from "@ant-design/icons";
import { Flex, Button } from "antd";

const TooltipDeleteBody = (
  handleDelete: () => void,
  setOpenTooltip: (value: boolean) => void
) => (
  <Flex vertical gap={20}>
    <Flex gap={12}>
      <Flex>
        <ExclamationCircleFilled style={{ color: "#FAAD14" }} />
      </Flex>
      <Flex vertical>
        <Text.NormalLarge color="black">
          Are you sure to delete this API server?
        </Text.NormalLarge>
        <Text.LightMedium color="rgba(0,0,0,0.45)">
          No endpoint in this API server is in use.
        </Text.LightMedium>
      </Flex>
    </Flex>
    <Flex justify="end" gap={12}>
      <Button onClick={() => setOpenTooltip(false)}>Cancel</Button>
      <Button style={{ color: "red" }} onClick={handleDelete}>
        Delete
      </Button>
    </Flex>
  </Flex>
);

export default TooltipDeleteBody;
