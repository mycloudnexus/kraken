import { useBoolean } from "usehooks-ts";
import Flex from "../Flex";
import { DownOutlined, RightOutlined } from "@ant-design/icons";
import { Text } from "../Text";
import { ReactNode } from "react";

type Props = {
  children: ReactNode;
  defaultValue: boolean;
  className?: string;
  title?: string;
  description?: string;
};

const ExpandCard = ({
  children,
  defaultValue = false,
  className,
  title,
  description,
}: Props) => {
  const { value: isOpen, toggle: toggleOpen } = useBoolean(defaultValue);
  return (
    <div className={className}>
      <Flex justifyContent="flex-start" gap={4} alignItems="center">
        {isOpen ? (
          <DownOutlined onClick={toggleOpen} style={{ fontSize: 10 }} />
        ) : (
          <RightOutlined onClick={toggleOpen} style={{ fontSize: 10 }} />
        )}
        <Text.NormalLarge>{title}</Text.NormalLarge>
      </Flex>
      <Text.LightMedium color="#00000073">{description}</Text.LightMedium>
      {isOpen && <div style={{ marginTop: 16 }}>{children}</div>}
    </div>
  );
};

export default ExpandCard;
