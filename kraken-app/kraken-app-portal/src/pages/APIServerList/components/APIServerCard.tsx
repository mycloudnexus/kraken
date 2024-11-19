import TitleIcon from "@/assets/title-icon.svg";
import DeleteApiButton from "@/components/DeleteApiButton";
import SpecDrawer from "@/components/SpecDrawer";
import { SecondaryText, Text } from "@/components/Text";
import { useDeleteApiServer } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IComponent } from "@/utils/types/component.type";
import { PaperClipOutlined } from "@ant-design/icons";
import {
  Button,
  Card,
  Col,
  Flex,
  Row,
  Spin,
  Tag,
  Typography,
  notification,
} from "antd";
import { decode } from "js-base64";
import jsYaml from "js-yaml";
import { get } from "lodash";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useBoolean } from "usehooks-ts";

type Props = {
  item: IComponent;
  refetchList?: () => void;
};

const APIServerCard = ({ item, refetchList }: Props) => {
  const { currentProduct } = useAppStore();
  const {
    value: isHover,
    setTrue: trueHover,
    setFalse: falseHover,
  } = useBoolean(false);
  const [openMappingDrawer, setOpenMappingDrawer] = useState(false);
  const { mutateAsync: deleteApiServer, isPending } = useDeleteApiServer();

  const navigate = useNavigate();
  const {
    value: isOpenDrawer,
    setTrue: openDrawer,
    setFalse: closeDrawer,
  } = useBoolean(false);
  const handleEdit = () => {
    navigate(`/component/${currentProduct}/edit/${get(item, "metadata.key")}`);
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

  const isApiInUse = useMemo(() => !!item?.inUse, [item]);

  return (
    <Spin spinning={isPending}>
      {isOpenDrawer && (
        <SpecDrawer
          onClose={closeDrawer}
          isOpen={isOpenDrawer}
          content={get(item, "facets.baseSpec.content")}
        />
      )}
      <Card
        style={{ borderRadius: 4, width: "100%" }}
        title={
          <Flex justify="space-between" gap={12} align="center">
            <Flex gap={8}>
              <Text.NormalLarge>
                {get(item, "metadata.name", "")}
              </Text.NormalLarge>
              <div onMouseEnter={trueHover} onMouseLeave={falseHover}>
                {isHover && isApiInUse ? (
                  <Button
                    style={{ padding: "0px" }}
                    type="link"
                    onClick={() => setOpenMappingDrawer(true)}
                  >
                    Check details
                  </Button>
                ) : (
                  <Tag color={isApiInUse ? "blue" : ""} >
                    <Text.LightSmall>
                      {isApiInUse ? "In use" : "Not in use"}
                    </Text.LightSmall>
                  </Tag>
                )}
              </div>
            </Flex>
            <Flex>
              <Button type="link" onClick={handleEdit}>
                Edit
              </Button>
              <DeleteApiButton
                openMappingDrawer={openMappingDrawer}
                deleteCallback={deleteApiServer}
                setOpenMappingDrawer={setOpenMappingDrawer}
                item={item}
                refetchList={refetchList}
              />
            </Flex>
          </Flex>
        }
      >
        <Row gutter={[16, 16]}>
          <Group title="Seller API Server basics">
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
                    title: get(item, "metadata.description", "-"),
                  },
                }}
              >
                {get(item, "metadata.description", "-")}
              </Typography.Text>
            </Flex>
          </Group>

          <Group title="Base URL for environment variables">
            {environmentData?.map((e) => (
              <Flex gap={8} vertical justify="flex-start" key={e.name}>
                <SecondaryText.LightNormal style={{ textTransform: 'capitalize' }}>
                  {e.name} URL
                </SecondaryText.LightNormal>
                <Typography.Text style={{ whiteSpace: "break-spaces" }}>
                  {e.url}
                </Typography.Text>
              </Flex>
            ))}
          </Group>

          <Group title="API spec">
            <Flex vertical gap={8}>
              <Text.NormalMedium color="#000000D9">
                API spec in yaml format
              </Text.NormalMedium>

              <Text.LightMedium
                color="#2962FF"
                style={{ cursor: "pointer" }}
                role="none"
                onClick={openDrawer}
              >
                <PaperClipOutlined style={{ color: "#000000D9" }} /> {fileName}
              </Text.LightMedium>
            </Flex>
          </Group>
        </Row>
      </Card>
    </Spin>
  );
};

export default APIServerCard;

function Group({ title, children }: Readonly<React.PropsWithChildren<{ title?: React.ReactNode }>>) {
  return (
    <Col lg={8} md={24}>
      <Flex gap={8} justify="flex-start" align="center">
        <TitleIcon />
        <Text.NormalLarge>{title}</Text.NormalLarge>
      </Flex>
      <Flex vertical align="flex-start" gap={20} style={{ marginTop: 12 }}>
        {children}
      </Flex>
    </Col>
  )
}
