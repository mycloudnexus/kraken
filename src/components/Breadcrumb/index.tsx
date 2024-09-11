import { LeftOutlined } from "@ant-design/icons";
import { Flex } from "antd";
import { useNavigate } from "react-router-dom";
import Text from "../Text";

type Props = {
  mainUrl: string;
  lastItem: string;
  items?: {
    title: string;
    url: string;
  }[];
};

const BreadCrumb = ({ mainUrl, lastItem, items }: Props) => {
  const navigate = useNavigate();
  return (
    <Flex gap={8} align="center">
      <Flex
        gap={8}
        align="center"
        style={{ cursor: "pointer" }}
        onClick={() => navigate(mainUrl)}
      >
        <LeftOutlined style={{ fontSize: 8 }} />
        <Text.LightLarge>Seller API setup</Text.LightLarge>
      </Flex>
      {items?.map((i) => (
        <Flex
          gap={8}
          align="center"
          style={{ cursor: "pointer" }}
          onClick={() => navigate(i.url)}
        >
          <Text.LightLarge>/</Text.LightLarge>
          <Text.LightLarge>{i.title}</Text.LightLarge>
        </Flex>
      ))}
      <Flex gap={8} align="center">
        <Text.LightLarge>/</Text.LightLarge>
        <Text.LightLarge color="#00000073">{lastItem}</Text.LightLarge>
      </Flex>
    </Flex>
  );
};

export default BreadCrumb;
