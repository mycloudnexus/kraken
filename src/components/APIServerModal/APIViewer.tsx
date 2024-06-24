import { Button, Col, Row, notification } from "antd";
import Text from "../Text";
import styles from "./index.module.scss";
import { useMemo } from "react";
import { get } from "lodash";
import Flex from "../Flex";
import { PaperClipOutlined } from "@ant-design/icons";
import TitleIcon from "@/assets/title-icon.svg";
import RequestMethod from "../Method";
import jsYaml from "js-yaml";
import { decode } from "js-base64";
import { useNavigate } from "react-router-dom";
import { useAppStore } from "@/stores/app.store";

type Props = {
  detail: any;
  onClose?: () => void;
  enableEdit?: () => void;
};

const APIViewer = ({ detail, enableEdit }: Props) => {
  const navigate = useNavigate();
  const { currentProduct } = useAppStore();
  const fileName = useMemo(() => {
    try {
      if (!get(detail, "facets.baseSpec.content")) {
        return "";
      }
      const fileData = decode(get(detail, "facets.baseSpec.content"));
      const swaggerData = jsYaml.load(fileData);
      return get(swaggerData, "info.title");
    } catch (error) {
      notification.error({ message: "Can not load yaml" });
    }
  }, [detail]);

  const environmentData = useMemo(() => {
    const env = get(detail, "facets.environments");
    if (!env) {
      return [];
    }
    const keys = Object.keys(env);
    return keys?.map((k: string) => ({ name: k, url: env[k] }));
  }, [detail]);

  return (
    <div>
      <div
        className={styles.paper}
        style={{
          flex: 1,
          display: "flex",
          flexDirection: "column",
          gap: 24,
          width: "100%",
          boxSizing: "border-box",
        }}
      >
        <Flex alignItems="flex-start" flexDirection="column" gap={12}>
          <Flex gap={8} justifyContent="flex-start">
            <TitleIcon />
            <Text.NormalLarge>Seller API Server basics</Text.NormalLarge>
            <Button
              type="text"
              style={{ color: "#2962FF" }}
              onClick={enableEdit}
            >
              Edit
            </Button>
          </Flex>
          <div>
            <Row gutter={[20, 20]}>
              <Col span={8}>
                <Flex flexDirection="column" alignItems="flex-start" gap={8}>
                  <Text.LightMedium color="#00000073">
                    Seller API Server Name
                  </Text.LightMedium>
                  <Text.LightMedium>
                    {get(detail, "metadata.name", "")}
                  </Text.LightMedium>
                </Flex>
              </Col>
              <Col span={16}>
                <Flex flexDirection="column" alignItems="flex-start" gap={8}>
                  <Text.LightMedium color="#00000073">
                    Online API document link
                  </Text.LightMedium>
                  <Text.LightMedium>
                    {get(detail, "metadata.link", "-")}
                  </Text.LightMedium>
                </Flex>
              </Col>
              <Col span={24}>
                <Flex flexDirection="column" alignItems="flex-start" gap={8}>
                  <Text.LightMedium color="#00000073">
                    Description
                  </Text.LightMedium>
                  <Text.LightMedium>
                    {get(detail, "metadata.description", "-")}
                  </Text.LightMedium>
                </Flex>
              </Col>
              <Col span={24}>
                <Flex flexDirection="column" alignItems="flex-start" gap={4}>
                  <Text.NormalMedium color="#000000D9">
                    API spec in yaml format
                  </Text.NormalMedium>
                  <Flex gap={9} justifyContent="flex-start">
                    <PaperClipOutlined />
                    <Text.LightMedium>{fileName}</Text.LightMedium>
                  </Flex>
                </Flex>
              </Col>
            </Row>
          </div>
        </Flex>
        <Flex alignItems="flex-start" flexDirection="column" gap={12}>
          <Flex gap={8} justifyContent="flex-start">
            <TitleIcon />
            <Text.NormalLarge>Seller API</Text.NormalLarge>
            <Button
              type="text"
              style={{ color: "#2962FF" }}
              onClick={() =>
                navigate(
                  `/component/${currentProduct}/edit/${get(
                    detail,
                    "metadata.key"
                  )}/api`
                )
              }
            >
              Edit
            </Button>
          </Flex>
          <Flex flexDirection="column" gap={8} alignItems="flex-start">
            {get(detail, "facets.selectedAPIs")?.map((api: string) => (
              <Flex key={api} gap={8} justifyContent="flex-start">
                <div style={{ width: 58 }}>
                  <RequestMethod method={get(api.split(" "), "[1]")} />
                </div>
                <Text.LightMedium>
                  {get(api.split(" "), "[0]")}
                </Text.LightMedium>
              </Flex>
            ))}
          </Flex>
        </Flex>
        <Flex flexDirection="column" gap={12} alignItems="flex-start">
          <Flex gap={8} justifyContent="flex-start">
            <TitleIcon />
            <Text.NormalLarge>
              Base URL for environment variables
            </Text.NormalLarge>
            <Button
              type="text"
              style={{ color: "#2962FF" }}
              onClick={enableEdit}
            >
              Edit
            </Button>
          </Flex>
          <Flex flexDirection="column" gap={8} alignItems="flex-start">
            {environmentData?.map((e) => (
              <Flex gap={8} justifyContent="flex-start" key={e.name}>
                <Text.LightMedium style={{ width: 120 }}>
                  {e.name}
                </Text.LightMedium>
                <Text.LightMedium>URL: {e.url}</Text.LightMedium>
              </Flex>
            ))}
          </Flex>
        </Flex>
      </div>
    </div>
  );
};

export default APIViewer;
