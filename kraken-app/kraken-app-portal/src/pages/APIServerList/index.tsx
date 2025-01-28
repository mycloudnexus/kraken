import Contacts from "@/assets/contacts.svg";
import OrderIcon from "@/assets/standardAPIMapping/order.svg";
import QuoteIcon from "@/assets/standardAPIMapping/quote.svg";
import Flex from "@/components/Flex";
import { Text } from "@/components/Text";
import { useGetSellerAPIList, useGetSellerContacts } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IComponent } from "@/utils/types/component.type";
import { CloseOutlined } from "@ant-design/icons";
import { Button, Card, Empty, Spin, Tabs, Row, Col, Tag, Drawer } from "antd";
import { isEmpty } from "lodash";
import { Dispatch, SetStateAction, useState } from "react";
import { useNavigate } from "react-router";
import APIServerCard from "./components/APIServerCard";
import styles from "./index.module.scss";

type DrawerDetails = {
  apiComponent: string;
  productType: string;
  contactName: string;
  email: string;
  phoneNumber: string;
};

const getCardTitle = (componentKey: string) => {
  switch (componentKey) {
    case "mef.sonata.api.quote":
      return "Quote Management";
    case "mef.sonata.api.order":
      return "Order Management";
    default:
      return "";
  }
};

const getCardIcon = (componentKey: string) => {
  switch (componentKey) {
    case "mef.sonata.api.quote":
      return <QuoteIcon />;
    case "mef.sonata.api.order":
      return <OrderIcon />;
    default:
      return <></>;
  }
};

const ContactInformationSetup = ({
  setDrawerOpen,
  setDrawerDetails,
}: {
  setDrawerOpen: Dispatch<SetStateAction<boolean>>;
  setDrawerDetails: Dispatch<SetStateAction<DrawerDetails>>;
}) => {
  const { currentProduct } = useAppStore();
  const { data: dataList } = useGetSellerContacts(currentProduct);

  return (
    <>
      {isEmpty(dataList?.data) ? (
        <Flex style={{ height: "50vh" }}>
          <Empty />
        </Flex>
      ) : (
        <Row gutter={16}>
          {dataList.data.map((item: IComponent) => (
            <Col key={item.id} span={12}>
              <Card
                title={
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                    }}
                  >
                    {getCardIcon(item.metadata.labels.componentKey)}
                    <Text.BoldMedium style={{ marginLeft: "10px" }}>
                      {getCardTitle(item.metadata.labels.componentKey)}
                    </Text.BoldMedium>
                    <Tag color="purple" style={{ marginLeft: "10px" }}>
                      {item.metadata.labels["access.eline"] === "true"
                        ? "Access Eline"
                        : "Internet Access"}
                    </Tag>
                  </div>
                }
                extra={
                  <Button
                    type="link"
                    onClick={() => {
                      setDrawerDetails({
                        apiComponent: getCardTitle(
                          item.metadata.labels.componentKey
                        ),
                        productType:
                          item.metadata.labels["access.eline"] === "true"
                            ? "Access Eline"
                            : "Internet Access",
                        contactName: item?.facets?.sellerInfo?.name || "",
                        email: item?.facets?.sellerInfo?.emailAddress || "",
                        phoneNumber: item?.facets?.sellerInfo?.number || "",
                      });
                      setDrawerOpen(true);
                    }}
                  >
                    Edit
                  </Button>
                }
              >
                <Row>
                  <Col span={16}>
                    <div
                      style={{
                        display: "flex",
                        flexDirection: "column",
                      }}
                    >
                      <Text.LightSmall
                        className={styles["contactInformation-field"]}
                        color="#00000073"
                      >
                        Contact name
                      </Text.LightSmall>
                      <Text.LightSmall
                        className={styles["contactInformation-field"]}
                      >
                        {item?.facets?.sellerInfo?.name || "-"}
                      </Text.LightSmall>
                      <Text.LightSmall
                        className={styles["contactInformation-field"]}
                        color="#00000073"
                      >
                        Email
                      </Text.LightSmall>
                      <Text.LightSmall
                        className={styles["contactInformation-field"]}
                      >
                        {item?.facets?.sellerInfo?.emailAddress || "-"}
                      </Text.LightSmall>
                      <Text.LightSmall
                        className={styles["contactInformation-field"]}
                        color="#00000073"
                      >
                        Phone number
                      </Text.LightSmall>
                      <Text.LightSmall
                        className={styles["contactInformation-field"]}
                      >
                        {item?.facets?.sellerInfo?.number || "-"}
                      </Text.LightSmall>
                    </div>
                  </Col>
                  <Col span={8} className={styles["contacts-icon"]}>
                    <Contacts />
                  </Col>
                </Row>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </>
  );
};

const APIServerList = () => {
  const { currentProduct } = useAppStore();
  const {
    data: dataList,
    isLoading,
    isRefetching,
    refetch,
  } = useGetSellerAPIList(currentProduct);
  const navigate = useNavigate();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [drawerDetails, setDrawerDetails] = useState<DrawerDetails>({
    apiComponent: "-",
    productType: "-",
    contactName: "-",
    email: "-",
    phoneNumber: "-",
  });

  return (
    <div style={{ width: "100%" }}>
      <Tabs
        className={styles["seller-api-tabs-container"]}
        defaultActiveKey="1"
        tabBarExtraContent={
          <Button
            type="primary"
            onClick={() => navigate(`/component/${currentProduct}/new`)}
          >
            + Create API server
          </Button>
        }
        items={[
          {
            key: "1",
            label: "API server setup",
            children: (
              <div className={styles.content}>
                <Spin spinning={isLoading || isRefetching}>
                  <Flex
                    flexDirection="column"
                    alignItems="flex-start"
                    gap={16}
                    style={{ width: "100%" }}
                  >
                    {dataList?.data?.map((item: IComponent) => (
                      <div key={item.id} style={{ width: "100%" }}>
                        <APIServerCard item={item} refetchList={refetch} />
                      </div>
                    ))}
                  </Flex>
                  {isEmpty(dataList?.data) && (
                    <Flex style={{ height: "50vh" }}>
                      <Empty />
                    </Flex>
                  )}
                </Spin>
              </div>
            ),
          },
          {
            key: "2",
            label: "Contact information setup",
            children: (
              <ContactInformationSetup
                setDrawerOpen={setDrawerOpen}
                setDrawerDetails={setDrawerDetails}
              />
            ),
          },
        ]}
      />
      <Drawer
        title={
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <Text.BoldMedium>Add contact information</Text.BoldMedium>
            <CloseOutlined onClick={() => setDrawerOpen(false)} />
          </div>
        }
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
      >
        {JSON.stringify(drawerDetails)}
      </Drawer>
    </div>
  );
};

export default APIServerList;
