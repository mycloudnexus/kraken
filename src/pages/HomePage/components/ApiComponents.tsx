import { Col, Row, Spin } from "antd";
import { useAppStore } from "@/stores/app.store";

import yaml from "js-yaml";
import { decode } from "js-base64";

import { useCallback } from "react";

import { useGetProductComponents } from "@/hooks/product";
import type { IUnifiedAsset } from "@/utils/types/common.type";
import { SPEC_VALUE } from "@/utils/constants/product";
import Text from "@/components/Text";
import ApiComponent from "./ApiComponent";
import { get, isEmpty } from "lodash";

const ApiComponents = () => {
  const { currentProduct } = useAppStore();

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
        decode(get(targetSpec, "facets.baseSpec.content", ""))
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
      i.facets?.supportedProductTypesAndActions?.forEach((s: any) => {
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
      <div style={{ margin: "24px 0 16px" }}>
        <Text.Custom size="20px" bold="500">
          Standard API components ({componentList?.data?.length ?? 0})
        </Text.Custom>
      </div>
      <Spin spinning={specLoading || isLoading}>
        <Row gutter={[24, 24]} style={{ marginBottom: 24 }}>
          {componentList?.data?.map((i: IUnifiedAsset, n: number) => {
            const { targetSpec = {}, targetYaml = {} } = getTargetSpecItem(i);
            const supportInfo = getSupportInfo(i);
            const title = targetYaml.info?.title;
            const apis =
              i?.facets?.supportedProductTypesAndActions?.length ?? 0;
            if (isEmpty(targetSpec)) {
              return null;
            }
            return (
              <Col lg={12} xl={8} sm={24} key={i.id}>
                <ApiComponent
                  item={i}
                  index={n}
                  title={title}
                  apis={apis}
                  targetSpec={targetSpec}
                  targetYaml={targetYaml}
                  supportInfo={supportInfo}
                  componentList={componentList}
                />
              </Col>
            );
          })}
        </Row>
      </Spin>
    </>
  );
};

export default ApiComponents;
