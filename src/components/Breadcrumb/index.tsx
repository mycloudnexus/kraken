import { LeftOutlined } from "@ant-design/icons";
import { Flex } from "antd";
import { useNavigate } from "react-router-dom";
import Text from "../Text";
import styles from "./index.module.scss";

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
        className={styles.canClick}
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
          className={styles.canClick}
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
