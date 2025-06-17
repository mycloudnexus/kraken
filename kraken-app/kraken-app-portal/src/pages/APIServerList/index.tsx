import Contacts from "@/assets/contacts.svg";
import OrderIcon from "@/assets/standardAPIMapping/order.svg";
import QuoteIcon from "@/assets/standardAPIMapping/quote.svg";
import Flex from "@/components/Flex";
import { Text } from "@/components/Text";
import {
  useGetSellerAPIList,
  useGetSellerContacts,
  useEditContactInformation,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IComponent } from "@/utils/types/component.type";
import { CloseOutlined } from "@ant-design/icons";
import {
  Button,
  Card,
  Empty,
  Spin,
  Tabs,
  Row,
  Col,
  Drawer,
  Input,
  notification,
  Alert,
} from "antd";
import { isEmpty } from "lodash";
import { Dispatch, SetStateAction, useState } from "react";
import { useNavigate } from "react-router";
import APIServerCard from "./components/APIServerCard";
import styles from "./index.module.scss";

type DrawerDetails = {
  apiComponent: string;
  productType: string;
  contactName?: string;
  email?: string;
  phoneNumber?: string;
  componentKey: string;
  key: string;
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
  sellerContactsList,
}: {
  setDrawerOpen: Dispatch<SetStateAction<boolean>>;
  setDrawerDetails: Dispatch<SetStateAction<DrawerDetails>>;
  sellerContactsList: IComponent[];
}) => {
  return (
    <>
      {isEmpty(sellerContactsList) ? (
        <Flex style={{ height: "50vh" }}>
          <Empty />
        </Flex>
      ) : (
        <Row gutter={[16, 16]}>
          {sellerContactsList.map((item: IComponent) => (
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
                        contactName: item?.facets?.sellerInfo?.name,
                        email: item?.facets?.sellerInfo?.emailAddress,
                        phoneNumber: item?.facets?.sellerInfo?.number,
                        componentKey: item?.metadata?.labels?.componentKey,
                        key: item?.metadata?.key,
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
                        className={styles["contactInformation-field-label"]}
                        color="#00000073"
                      >
                        Contact name
                      </Text.LightSmall>
                      <Text.LightSmall
                        className={styles["contactInformation-field-value"]}
                      >
                        {item?.facets?.sellerInfo?.name || "-"}
                      </Text.LightSmall>
                      <Text.LightSmall
                        className={styles["contactInformation-field-label"]}
                        color="#00000073"
                      >
                        Email
                      </Text.LightSmall>
                      <Text.LightSmall
                        className={styles["contactInformation-field-value"]}
                      >
                        {item?.facets?.sellerInfo?.emailAddress || "-"}
                      </Text.LightSmall>
                      <Text.LightSmall
                        className={styles["contactInformation-field-label"]}
                        color="#00000073"
                      >
                        Phone number
                      </Text.LightSmall>
                      <Text.LightSmall>
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
  const { data: sellerContactsListResponse, refetch: contactsListRefetch } =
    useGetSellerContacts(currentProduct);
  const { mutateAsync: updateContactInformation } = useEditContactInformation();
  const navigate = useNavigate();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [drawerDetails, setDrawerDetails] = useState<DrawerDetails>({
    apiComponent: "-",
    productType: "-",
    componentKey: "-",
    key: "-",
  });
  const [activeKey, setActiveKey] = useState("apiServerSetup");

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setDrawerDetails({
      ...drawerDetails,
      [e.currentTarget.name]: e.currentTarget.value,
    });
  };

  return (
    <div style={{ width: "100%" }}>
      <Tabs
        className={styles["seller-api-tabs-container"]}
        onChange={(key) => setActiveKey(key)}
        tabBarExtraContent={
          activeKey === "apiServerSetup" && (
            <Button
              type="primary"
              onClick={() => navigate(`/component/${currentProduct}/new`)}
            >
              + Create API server
            </Button>
          )
        }
        items={[
          {
            key: "apiServerSetup",
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
            key: "contactInformationSetup",
            label: "Contact information setup",
            children: (
              <ContactInformationSetup
                setDrawerOpen={setDrawerOpen}
                setDrawerDetails={setDrawerDetails}
                sellerContactsList={sellerContactsListResponse?.data}
              />
            ),
          },
        ]}
      />
      <Drawer
        title={
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <Text.BoldMedium>Edit contact information</Text.BoldMedium>
            <CloseOutlined onClick={() => setDrawerOpen(false)} />
          </div>
        }
        open={drawerOpen}
        maskClosable={false}
        onClose={() => setDrawerOpen(false)}
        width={578}
        destroyOnClose
        footer={
          <div style={{ display: "flex", justifyContent: "flex-end" }}>
            <Button
              style={{ margin: "5px" }}
              onClick={() => setDrawerOpen(false)}
            >
              Cancel
            </Button>
            <Button
              style={{ margin: "5px" }}
              type="primary"
              onClick={async () => {
                try {
                  await updateContactInformation({
                    productId: currentProduct,
                    componentId: drawerDetails.componentKey,
                    id: drawerDetails.key,
                    data: {
                      name: drawerDetails.contactName,
                      emailAddress: drawerDetails.email,
                      number: drawerDetails.phoneNumber,
                    },
                  } as any).then(contactsListRefetch);
                  notification.success({
                    message: "Updated contact information",
                    duration: 3,
                  });
                } catch (e) {
                  notification.error({
                    message: "Something went wrong",
                    duration: 3,
                  });
                } finally {
                  setDrawerOpen(false);
                }
              }}
            >
              Ok
            </Button>
          </div>
        }
      >
        <div
          style={{
            display: "flex",
            flexDirection: "column",
          }}
        >
          <Alert
            className={styles.alert}
            message={
              <span style={{ fontSize: "12px" }}>
                To pass MEF certificate, all below three contact fields need to
                be filled.
              </span>
            }
            type="warning"
            showIcon
            closable
          />
          <Text.LightSmall
            className={styles["contactInformation-field-label"]}
            color="#00000073"
          >
            API component
          </Text.LightSmall>
          <Text.LightSmall className={styles["contactInformation-field-value"]}>
            {drawerDetails.apiComponent || "-"}
          </Text.LightSmall>
          <Text.LightSmall className={styles["contactInformation-field-label"]}>
            Contact name
          </Text.LightSmall>
          <Input
            name="contactName"
            value={drawerDetails?.contactName}
            onChange={handleInputChange}
            placeholder="Please enter"
            style={{ marginBottom: "20px" }}
          />
          <Text.LightSmall className={styles["contactInformation-field-label"]}>
            Email
          </Text.LightSmall>
          <Input
            name="email"
            value={drawerDetails?.email}
            onChange={handleInputChange}
            placeholder="Please enter"
            style={{ marginBottom: "20px" }}
          />
          <Text.LightSmall className={styles["contactInformation-field-label"]}>
            Phone number
          </Text.LightSmall>
          <Input
            name="phoneNumber"
            value={drawerDetails?.phoneNumber}
            onChange={handleInputChange}
            placeholder="Please enter"
            style={{ marginBottom: "20px" }}
          />
        </div>
      </Drawer>
    </div>
  );
};

export default APIServerList;
