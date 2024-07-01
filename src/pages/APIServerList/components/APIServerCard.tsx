import APIServerModal from "@/components/APIServerModal";
import Text from "@/components/Text";
import { IComponent } from "@/utils/types/product.type";
import { Card, Col, Flex, Row, Tag, Typography, notification } from "antd";
import { get } from "lodash";
import { useBoolean } from "usehooks-ts";
import TitleIcon from "@/assets/title-icon.svg";
import { useMemo } from "react";
import { PaperClipOutlined } from "@ant-design/icons";
import jsYaml from "js-yaml";
import { decode } from "js-base64";
import SpecDrawer from "@/components/SpecDrawer";

type Props = {
  item: IComponent;
  refresh: () => void;
};

const APIServerCard = ({ item, refresh }: Props) => {
  const {
    value: isOpenModal,
    setTrue: openModal,
    setFalse: closeModal,
  } = useBoolean(false);
  const {
    value: isOpenDrawer,
    setTrue: openDrawer,
    setFalse: closeDrawer,
  } = useBoolean(false);
  const handleEdit = () => {
    openModal();
  };

  const environmentData = useMemo(() => {
    const env = get(item, "facets.environments");
    if (!env) {
      return [];
    }
    const keys = Object.keys(env);
    return keys?.map((k: string) => ({ name: k, url: env[k] }));
  }, [item]);

  const fileName = useMemo(() => {
    try {
      const content = get(item, "facets.baseSpec.content");
      if (!content) {
        return "";
      }
      const swaggerData = jsYaml.load(decode(content));
      return get(swaggerData, "info.title");
    } catch (error) {
      notification.error({ message: "Can not load yaml" });
    }
  }, [item]);

  return (
    <>
      {isOpenDrawer && (
        <SpecDrawer
          onClose={closeDrawer}
          isOpen={isOpenDrawer}
          content={get(item, "facets.baseSpec.content")}
        />
      )}
      {isOpenModal && (
        <APIServerModal
          id={item?.metadata?.key}
          isOpen={isOpenModal}
          onClose={closeModal}
          refresh={refresh}
        />
      )}
      <Card
        style={{ borderRadius: 4, width: "100%" }}
        title={
          <Flex justify="flex-start" gap={12} align="center">
            <Text.NormalLarge>
              {get(item, "metadata.name", "")}
            </Text.NormalLarge>
            <Text.LightMedium
              color="#2962FF"
              style={{ cursor: "pointer" }}
              role="none"
              onClick={handleEdit}
            >
              Edit
            </Text.LightMedium>
          </Flex>
        }
      >
        <Row gutter={[24, 12]}>
          <Col lg={8} md={12}>
            <Flex gap={8} justify="flex-start" align="center">
              <TitleIcon />
              <Text.NormalLarge>Seller API Server basics</Text.NormalLarge>
            </Flex>
            <Flex vertical gap={12} style={{ marginTop: 12 }}>
              <Flex vertical align="flex-start" gap={8}>
                <Text.LightMedium color="#00000073">
                  Application name
                </Text.LightMedium>
                <Typography.Text
                  ellipsis={{
                    tooltip: { title: get(item, "metadata.name") },
                  }}
                >
                  {get(item, "metadata.name")}
                </Typography.Text>
                <Text.LightMedium></Text.LightMedium>
              </Flex>
              <Flex vertical align="flex-start" gap={8}>
                <Text.LightMedium color="#00000073">
                  Online API document link
                </Text.LightMedium>
                <Typography.Text
                  ellipsis={{
                    tooltip: { title: get(item, "facets.baseSpec.path", "-") },
                  }}
                >
                  {get(item, "facets.baseSpec.path", "-")}
                </Typography.Text>
              </Flex>
              <Flex vertical align="flex-start" gap={8}>
                <Text.LightMedium color="#00000073">
                  Description
                </Text.LightMedium>
                <Typography.Text
                  ellipsis={{
                    tooltip: {
                      title: get(item, "metadata.name", "description"),
                    },
                  }}
                >
                  {get(item, "metadata.name", "description")}
                </Typography.Text>
              </Flex>
            </Flex>
          </Col>
          <Col lg={8} md={12}>
            <Flex gap={8} justify="flex-start" align="center">
              <TitleIcon />
              <Text.NormalLarge>
                Base URL for environment variables
              </Text.NormalLarge>
            </Flex>
            <Flex vertical gap={8} align="flex-start" style={{ marginTop: 12 }}>
              {environmentData?.map((e) => (
                <Flex gap={8} justify="flex-start" key={e.name}>
                  <Text.LightMedium style={{ width: 120 }}>
                    {e.name}
                  </Text.LightMedium>
                  <Typography.Text style={{ whiteSpace: "break-spaces" }}>
                    URL: {e.url}
                  </Typography.Text>
                </Flex>
              ))}
            </Flex>
          </Col>
          <Col lg={8} md={24}>
            <Flex gap={8} justify="flex-start" align="center">
              <TitleIcon />
              <Text.NormalLarge>API spec</Text.NormalLarge>
            </Flex>
            <Flex vertical align="flex-start" gap={6} style={{ marginTop: 12 }}>
              <Flex gap={8}>
                <Text.NormalMedium color="#000000D9">
                  API spec in yaml format
                </Text.NormalMedium>
                <Tag>
                  {get(item, "facets.selectedAPIs", []).length} APIs in use
                </Tag>
              </Flex>
              <Flex gap={9} justify="flex-start">
                <PaperClipOutlined />
                <Text.LightMedium
                  color="#2962FF"
                  style={{ cursor: "pointer" }}
                  role="none"
                  onClick={openDrawer}
                >
                  {fileName}
                </Text.LightMedium>
              </Flex>
            </Flex>
          </Col>
        </Row>
      </Card>
    </>
  );
};

export default APIServerCard;
