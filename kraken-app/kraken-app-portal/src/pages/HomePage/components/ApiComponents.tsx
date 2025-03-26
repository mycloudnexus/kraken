import { PageLayout } from "@/components/Layout";
import {
  useGetComponentListAPI,
  useGetComponentListSpec,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { SPEC_VALUE } from "@/utils/constants/product";
import type { IUnifiedAsset } from "@/utils/types/common.type";
import { CloseOutlined } from "@ant-design/icons";
import { Tabs, Card, Col, Drawer, Flex, Row, Spin } from "antd";
import { decode } from "js-base64";
import yaml from "js-yaml";
import { get, isEmpty, isNull } from "lodash";
import { useCallback, useState } from "react";
import ApiComponent from "./ApiComponent";

export type DrawerDetails = {
  apiTitle: string;
  documentTitle: string;
  documentContent: string;
};

const TAB_LABELS = {
  ACCESS_E_LINE: "Access Eline",
  INTERNET_ACCESS: "Internet Access",
  UNI: "Uni",
  SHARE: "Shared",
};

const ApiComponents = () => {
  const { currentProduct } = useAppStore();
  const [open, setOpen] = useState(false);
  const [drawerDetails, setDrawerDetails] = useState<DrawerDetails>({
    apiTitle: "",
    documentTitle: "",
    documentContent: "",
  });

  const { data: componentList, isLoading } =
    useGetComponentListAPI(currentProduct);

  const { data: componentWithSpec, isLoading: specLoading } =
    useGetComponentListSpec(currentProduct);

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

  const openDrawer = (details: DrawerDetails) => {
    setDrawerDetails(details);
    setOpen(true);
  };

  const closeDrawer = () => {
    setOpen(false);
  };

  return (
    <PageLayout title={`Standard API Mapping`}>
      <Spin spinning={specLoading || isLoading}>
        <Card style={{ height: "100%", borderRadius: "4px" }}>
          <Tabs>
            {Object.entries(TAB_LABELS).map(([key, label]) => (
              <Tabs.TabPane tab={label} key={key}>
                <Row gutter={[24, 24]}>
                  {componentList?.data?.map((i: IUnifiedAsset) => {
                    const { targetSpec = {}, targetYaml = {} } =
                      getTargetSpecItem(i);
                    const supportInfo = getSupportInfo(i);
                    const title = targetYaml.info?.title;
                    const apis =
                      i?.facets?.supportedProductTypesAndActions?.length ?? 0;
                    if (isEmpty(targetSpec) || isNull(supportInfo)) {
                      return null;
                    }
                    return (
                      <>
                        {supportInfo[0].includes(key) && (
                          <Col lg={12} xl={8} sm={24} key={i.id}>
                            <ApiComponent
                              item={i}
                              title={title}
                              apis={apis}
                              targetSpec={targetSpec}
                              targetYaml={targetYaml}
                              supportInfo={key}
                              openDrawer={openDrawer}
                            />
                          </Col>
                        )}
                      </>
                    );
                  })}
                </Row>
              </Tabs.TabPane>
            ))}
          </Tabs>
        </Card>
        <Drawer
          width={576}
          title={
            <Flex justify="space-between">
              {drawerDetails.apiTitle}
              <CloseOutlined onClick={closeDrawer} />
            </Flex>
          }
          onClose={closeDrawer}
          open={open}
        >
          <b>{drawerDetails.documentTitle}</b>
          <p style={{ whiteSpace: "pre-wrap", margin: "0" }}>
            {drawerDetails.documentContent}
          </p>
        </Drawer>
      </Spin>
    </PageLayout>
  );
};

export default ApiComponents;
