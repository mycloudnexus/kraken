import { PageLayout } from "@/components/Layout";
import {
  useGetProductTypeList,
  useGetStandardApiComponents,
} from "@/hooks/homepage";
import { useAppStore } from "@/stores/app.store";
import { StandardApiComponent } from "@/utils/types/product.type";
import { CloseOutlined } from "@ant-design/icons";
import { Tabs, Card, Col, Drawer, Flex, Row, Spin } from "antd";
import { decode } from "js-base64";
import yaml from "js-yaml";
import { useCallback, useState } from "react";
import ApiComponent from "./ApiComponent";

export type DrawerDetails = {
  apiTitle: string;
  documentTitle: string;
  documentContent: string;
};

const ApiComponents = () => {
  const { currentProduct } = useAppStore();
  const [open, setOpen] = useState(false);
  const [drawerDetails, setDrawerDetails] = useState<DrawerDetails>({
    apiTitle: "",
    documentTitle: "",
    documentContent: "",
  });

  const [selectedProductType, setSelectedProductType] = useState<
    string | undefined
  >(undefined);

  const { data: productTypeList } = useGetProductTypeList(currentProduct);

  const { data: standardApiComponents, isLoading: apiLoading } =
    useGetStandardApiComponents(currentProduct, selectedProductType || "UNI");

  const getTargetSpecItem = useCallback(
    (i: any) => {
      const targetYaml = yaml.load(decode(i)) as any;
      return { targetYaml };
    },
    [standardApiComponents]
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
      <Spin spinning={apiLoading}>
        <Card style={{ height: "100%", borderRadius: "4px" }}>
          <Tabs onChange={(key) => setSelectedProductType(key)}>
            {productTypeList?.map(
              (item: { split: (arg0: string) => [any, any] }) => {
                const [key, label] = item.split(":");
                return (
                  <Tabs.TabPane tab={label} key={key}>
                    <Row gutter={[24, 24]}>
                      {standardApiComponents?.map(
                        (component: StandardApiComponent) => {
                          const { name, componentKey, apiCount, baseSpec } =
                            component;
                          const { targetYaml = {} } = getTargetSpecItem(
                            baseSpec.content
                          );

                          if (!component.supportedProductTypes.includes(key))
                            return null;

                          return (
                            <Col lg={12} xl={8} sm={24} key={componentKey}>
                              <ApiComponent
                                item={component}
                                title={name}
                                apis={apiCount}
                                targetYaml={targetYaml}
                                supportInfo={key}
                                openDrawer={openDrawer}
                              />
                            </Col>
                          );
                        }
                      )}
                    </Row>
                  </Tabs.TabPane>
                );
              }
            )}
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
