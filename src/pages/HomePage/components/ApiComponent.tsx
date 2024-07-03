import { Col, Row, Tooltip, Divider, Flex, Typography } from "antd";
import { MoreIcon } from "./Icon";
import styles from "./index.module.scss";
import { ReactNode, useCallback, useMemo } from "react";
import { IUnifiedAsset } from "@/utils/types/common.type";
import { useGetRunningVersionList } from "@/hooks/product";
import { useNavigate } from "react-router-dom";
import { useAppStore } from "@/stores/app.store";
import clsx from "clsx";
import Text from "@/components/Text";

type Props = {
  targetSpec: Record<string, any>;
  targetYaml: Record<string, any>;
  supportInfo: any;
  componentList: {
    data: any[];
  };
  apis: number;
  title: string;
  index: number;
  item: IUnifiedAsset;
};

const ApiComponent = ({
  targetSpec,
  supportInfo,
  componentList,
  apis,
  title,
  targetYaml,
  index,
  item,
}: Props) => {
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();
  const componentIdList = useMemo(() => {
    return (
      componentList?.data?.map((m: IUnifiedAsset) => m?.metadata?.key) ?? []
    );
  }, [componentList]);

  const runningVersionsRes = useGetRunningVersionList({
    componentIds: componentIdList,
    productId: currentProduct,
  });

  const runningList = useMemo(() => {
    return runningVersionsRes?.map((r) => r.data ?? []) ?? [];
  }, [runningVersionsRes]);

  const getTextDom = useCallback(
    (dom: ReactNode, needTips = false, title: ReactNode = <></>) => {
      if (!needTips) {
        return dom;
      }
      return (
        <Tooltip title={title} overlayClassName={styles.tipsOverlay}>
          {dom}
        </Tooltip>
      );
    },
    []
  );
  return (
    <div className={styles.apiContainer}>
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
                fontSize: 20,
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
              <Text.LightSmall color="#DDE1E5">|</Text.LightSmall>
              <Text.LightMedium
                role="none"
                color="#2962FF"
                style={{ cursor: "pointer" }}
                onClick={() => navigate(`/api-mapping/${item.metadata.key}`)}
              >
                Mapping
                <MoreIcon />
              </Text.LightMedium>
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
        <p className={styles.desc}>{targetYaml.info?.description}</p>,
        targetYaml.info?.description?.length > 310,
        targetYaml.info?.description
      )}
      <Divider />
      <Flex className={styles.typeInfo} align="center" gap={4}>
        Product type
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

      <Divider />
      <Row>
        {runningList[index] &&
          (runningList[index] as any)?.map((r: any) => {
            return (
              <Col
                className={clsx(styles.tags, styles.envTag)}
                style={{ padding: "4px 8px" }}
                key={r.id}
              >
                {r.env?.name} <span>{r?.version ?? "N/A"}</span>
              </Col>
            );
          })}
      </Row>
    </div>
  );
};

export default ApiComponent;
