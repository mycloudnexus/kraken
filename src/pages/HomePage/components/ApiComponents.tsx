import styles from "./index.module.scss";
import { Col, Row, Divider, Flex, Tooltip, Spin } from "antd";
import { useAppStore } from "@/stores/app.store";
import classes from "classnames";
import yaml from "js-yaml";
import { decode } from "js-base64";
import { MoreIcon } from "./Icon";
import { ReactNode, useCallback, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import {
  useGetProductComponents,
  useGetRunningVersionList,
} from "@/hooks/product";
import type { IUnifiedAsset } from "@/utils/types/common.type";
import { SPEC_VALUE } from "@/utils/constants/product";

const ApiComponents = () => {
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();
  const { data: componentList, isLoading } = useGetProductComponents(
    currentProduct,
    {
      kind: "kraken.component.api",
    }
  );
  const { data: componentWithSpec, isLoading: specLoading } =
    useGetProductComponents(currentProduct, {
      kind: "kraken.component.api-spec",
    });
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

  const getTargetSpecItem = useCallback(
    (i: IUnifiedAsset) => {
      if (!componentList.data?.length || !componentWithSpec?.data?.length)
        return {};
      const targetAssetKey = i.links.find(
        (l) => l.relationship === SPEC_VALUE
      )?.targetAssetKey;

      const targetSpec = componentWithSpec?.data.find(
        (s: IUnifiedAsset) => s.metadata.key === targetAssetKey
      );

      const targetYaml = yaml.load(
        decode(targetSpec.facets.baseSpec.content)
      ) as any;
      return { targetSpec, targetYaml };
    },
    [componentList, componentWithSpec]
  );
  const getSupportInfo = useCallback(
    (i: IUnifiedAsset) => {
      if (!i.facets?.supportedProductTypesAndActions) return null;
      const actionTypesArr: string[] = [];
      const productTypesArr: string[] = [];
      i.facets.supportedProductTypesAndActions.forEach((s: any) => {
        actionTypesArr.push(...(s?.actionTypes ?? []));
        productTypesArr.push(...(s?.productTypes ?? []));
      });
      return [
        Array.from(new Set(productTypesArr)),
        Array.from(new Set(actionTypesArr)),
      ];
    },
    [componentList, componentWithSpec]
  );

  return (
    <>
      <h2>Standard API components {componentList?.data?.length}</h2>
      <Spin spinning={specLoading || isLoading}>
        <Flex wrap>
          {componentList?.data?.map((i: IUnifiedAsset, n: number) => {
            const { targetSpec = {}, targetYaml = {} } = getTargetSpecItem(i);
            const supportInfo = getSupportInfo(i);
            const title = targetYaml.info?.title;
            const apis = i.facets.supportedProductTypesAndActions?.length ?? 0;
            return (
              <Col className={styles.apiContainer} key={i.id}>
                {
                  <Row justify={"space-between"} align={"top"}>
                    <Col span={19}>
                      <Row style={{ width: "100%" }}>
                        <img src={targetSpec.metadata?.logo} alt={title}></img>
                        <Col className={styles.titleContainer}>
                          <Row className={styles.title} align={"middle"}>
                            {getTextDom(
                              <b>{title}</b>,
                              title?.length > 18,
                              title
                            )}
                            <span>{apis}</span> Api
                          </Row>
                          <Row>
                            {Object.keys(i.metadata.labels).map((l) => {
                              return (
                                <Tooltip title={l} key={l}>
                                  <Col className={styles.tags}>
                                    {i.metadata.labels[l]}
                                  </Col>
                                </Tooltip>
                              );
                            })}
                          </Row>
                        </Col>
                      </Row>
                    </Col>

                    <Col
                      style={{ color: "#2962FF", cursor: "pointer" }}
                      onClick={() => navigate(`/api-mapping/${i.metadata.key}`)}
                    >
                      mapping
                      <MoreIcon />
                    </Col>
                  </Row>
                }

                {getTextDom(
                  <p className={styles.desc}>{targetYaml.info?.description}</p>,
                  targetYaml.info?.description?.length > 310,
                  targetYaml.info?.description
                )}
                <Divider />
                <Row className={styles.typeInfo}>
                  Product type
                  {supportInfo ? (
                    supportInfo?.[0].map((s) => {
                      return (
                        <Col key={s} className={styles.tagInfo}>
                          {s}
                        </Col>
                      );
                    })
                  ) : (
                    <Col>NA</Col>
                  )}
                </Row>

                <Divider />
                <Row>
                  {runningList[n] &&
                    (runningList[n] as any)?.map((r: any) => {
                      return (
                        <Col
                          className={classes(styles.tags, styles.envTag)}
                          style={{ padding: "4px 8px" }}
                          key={r.id}
                        >
                          {r.env?.name} <span>{r?.version ?? "N/A"}</span>
                        </Col>
                      );
                    })}
                </Row>
              </Col>
            );
          })}
        </Flex>
      </Spin>
    </>
  );
};

export default ApiComponents;
