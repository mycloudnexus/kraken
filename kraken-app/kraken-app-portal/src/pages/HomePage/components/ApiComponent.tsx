import { Text } from "@/components/Text";
import { IUnifiedAsset } from "@/utils/types/common.type";
import { Col, Tooltip, Divider, Flex, Typography, Button } from "antd";
import { ReactNode, useCallback, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { DrawerDetails } from "./ApiComponents";
import styles from "./index.module.scss";

type Props = {
  targetSpec: Record<string, any>;
  targetYaml: Record<string, any>;
  supportInfo: any;
  apis: number;
  title: string;
  item: IUnifiedAsset;
  openDrawer: (details: DrawerDetails) => void;
};

const ApiComponent = ({
  targetSpec,
  supportInfo,
  apis,
  title,
  targetYaml,
  item,
  openDrawer,
}: Props) => {
  const navigate = useNavigate();

  const formattedTargetYaml = useMemo(() => {
    const splitDescription = targetYaml.info.description.split("**");
    return {
      title: splitDescription[1],
      description: splitDescription[2],
    };
  }, [targetYaml]);

  const getTextDom = useCallback((dom: ReactNode, needTips = false) => {
    if (!needTips) {
      return dom;
    }
    return (
      <div>
        {dom}
        <Button
          type="link"
          style={{ padding: "0px" }}
          onClick={(e) => {
            e.stopPropagation();
            openDrawer({
              apiTitle: title,
              documentTitle: formattedTargetYaml.title,
              documentContent: formattedTargetYaml.description,
            });
          }}
        >
          See more
        </Button>
      </div>
    );
  }, []);

  return (
    <Flex
      className={styles.apiContainer}
      onClick={() => {
        navigate(`/api-mapping/${item.metadata.key}`);
      }}
    >
      <div>
        <Flex justify="flex-start" gap={12} align="center">
          <img src={targetSpec.metadata?.logo} alt={title}></img>
          <Flex style={{ flex: 1, overflow: "hidden" }} vertical>
            <Flex
              style={{ flex: 1, width: "100%" }}
              justify="space-between"
              align="center"
              gap={12}
            >
              <Typography.Text
                style={{
                  fontSize: 16,
                  fontWeight: 500,
                }}
                ellipsis={{
                  tooltip: { title },
                }}
              >
                {title}
              </Typography.Text>
              <Flex
                justify="flex-end"
                gap={8}
                align="center"
                wrap="nowrap"
                style={{ whiteSpace: "nowrap" }}
              >
                <Text.LightSmall>{apis} APIs</Text.LightSmall>
              </Flex>
            </Flex>
            <Flex gap={8} style={{ marginTop: 2 }}>
              {Object.keys(item.metadata.labels).map((l) => {
                return (
                  <Tooltip title={l} key={l}>
                    <Col className={styles.tags}>{item.metadata.labels[l]}</Col>
                  </Tooltip>
                );
              })}
            </Flex>
          </Flex>
        </Flex>
        {getTextDom(
          <p className={styles.desc}>{formattedTargetYaml.description}</p>,
          targetYaml.info?.description?.length > 310
        )}
      </div>
      <div>
        <Divider />
        <Flex className={styles.typeInfo} align="center" gap={4}>
          <span style={{ color: "#717788" }}>Product type</span>
          {supportInfo ? (
            supportInfo?.[0].map((s: any) => {
              return (
                <Col key={s} className={styles.tagInfo}>
                  {s}
                </Col>
              );
            })
          ) : (
            <Col>NA</Col>
          )}
        </Flex>
      </div>
    </Flex>
  );
};

export default ApiComponent;
