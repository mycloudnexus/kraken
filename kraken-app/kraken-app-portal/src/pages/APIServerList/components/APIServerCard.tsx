import TitleIcon from "@/assets/title-icon.svg";
import DeleteApiButton from "@/components/DeleteApiButton";
import { SecondaryText, Text } from "@/components/Text";
import { useDeleteApiServer } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IComponent } from "@/utils/types/component.type";
import { Button, Card, Col, Flex, Row, Spin, Tag, Typography } from "antd";
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

  const isApiInUse = useMemo(() => !!item?.inUse, [item]);

  return (
    <Spin spinning={isPending}>
      <Card
        style={{ borderRadius: 4, width: "100%" }}
        title={
          <Flex justify="space-between" gap={12} align="center">
            <Flex gap={8} align="center">
              <Text.NormalLarge>
                {get(item, "metadata.name", "")}
              </Text.NormalLarge>
              <Flex onMouseEnter={trueHover} onMouseLeave={falseHover}>
                {isHover && isApiInUse ? (
                  <Button
                    style={{ padding: "5px 0" }}
                    type="link"
                    onClick={() => setOpenMappingDrawer(true)}
                  >
                    Check details
                  </Button>
                ) : (
                  <Tag color={isApiInUse ? "blue" : ""}>
                    <Text.LightSmall>
                      {isApiInUse ? "In use" : "Not in use"}
                    </Text.LightSmall>
                  </Tag>
                )}
              </Flex>
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
              <Text.LightMedium color="#00000073">Description</Text.LightMedium>
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

          <Group title="Base URL of Environments">
            {environmentData?.map((e) => (
              <Flex gap={8} vertical justify="flex-start" key={e.name}>
                <SecondaryText.LightNormal
                  style={{ textTransform: "capitalize" }}
                >
                  {e.name} URL
                </SecondaryText.LightNormal>
                <Typography.Text style={{ whiteSpace: "break-spaces" }}>
                  {e.url}
                </Typography.Text>
              </Flex>
            ))}
          </Group>
        </Row>
      </Card>
    </Spin>
  );
};

export default APIServerCard;

function Group({
  title,
  children,
}: Readonly<React.PropsWithChildren<{ title?: React.ReactNode }>>) {
  return (
    <Col lg={12} md={24}>
      <Flex gap={8} justify="flex-start" align="center">
        <TitleIcon />
        <Text.NormalLarge>{title}</Text.NormalLarge>
      </Flex>
      <Flex vertical align="flex-start" gap={20} style={{ marginTop: 12 }}>
        {children}
      </Flex>
    </Col>
  );
}
