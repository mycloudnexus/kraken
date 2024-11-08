import { Text } from "@/components/Text";
import useUser from "@/hooks/user/useUser";
import { Flex } from "antd";
import dayjs from "dayjs";
import { isEmpty } from "lodash";
import { useMemo } from "react";

export const ContentTime = ({ content = "", time = "" }) => {
  const { findUserName } = useUser();
  const userName = useMemo(() => findUserName(content), [findUserName]);
  return (
    <Flex vertical gap={2}>
      <Text.LightMedium data-testid="username">{userName}</Text.LightMedium>
      {!isEmpty(time) && (
        <Text.LightSmall data-testid="time" color="#00000073">
          {dayjs.utc(time).local().format("YYYY-MM-DD HH:mm:ss")}
        </Text.LightSmall>
      )}
    </Flex>
  );
};
