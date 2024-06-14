import Text from "@/components/Text";
import styles from "./index.module.scss";
import { Col, Row, Spin } from "antd";
import HomePageCard from "./components/HomePageCard";
import AddressIcon from "@/assets/home/address.svg";
import OrderIcon from "@/assets/home/order.svg";
import ProductIcon from "@/assets/home/product.svg";
import QuoteIcon from "@/assets/home/quote.svg";
import { useAppStore } from "@/stores/app.store";
import { useGetProductComponents } from "@/hooks/product";
import { IComponent } from "@/utils/types/product.type";
import { get } from "lodash";
import { useCallback } from "react";
const HomePage = () => {
  const { currentProduct } = useAppStore();
  const { data: componentList, isLoading } = useGetProductComponents(
    currentProduct,
    {
      size: 100,
      page: 0,
      kind: "kraken.component.api",
      facetIncluded: false,
    }
  );
  const icon = useCallback((text: string) => {
    const newText = text.toLocaleLowerCase();
    if (newText.includes("order")) {
      return <OrderIcon />;
    }
    if (newText.includes("product")) {
      return <ProductIcon />;
    }
    if (newText.includes("address")) {
      return <AddressIcon />;
    }
    return <QuoteIcon />;
  }, []);

  return (
    <div className={styles.homePage}>
      <div className={styles.container}>
        <div>
          <Text.Custom
            bold="500"
            size="38px"
            color="linear-gradient(97.62deg, #212F35 1.82%, #61899B 117.59%)"
          >
            MEF LSO Sonata Adaptors
          </Text.Custom>
        </div>
        <Spin
          spinning={isLoading}
          size="large"
          style={
            isLoading
              ? { height: 500, display: "flex", alignItems: "center" }
              : {}
          }
        >
          <div className={styles.cardWrapper}>
            <Row gutter={[38, 38]}>
              {componentList?.data?.map((item: IComponent) => (
                <Col md={12} sm={24} key={item?.id}>
                  <HomePageCard
                    id={get(item, "metadata.key", "")}
                    version={get(item, "metadata.labels.mef-api-release", "")}
                    icon={icon(get(item, "metadata.name", ""))}
                    title={item?.metadata?.name}
                    description="Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec vehicula posuere turpis at dictum. Aliquam ut mi a leo mattis consectetur. Suspendisse vitae efficitur ante. Praesent sed lectus pretium, dapibus metus eget, venenatis augue. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Etiam dapibus leo tellus, quis euismod nibh fermentum nec. Proin et fermentum magna"
                  />
                </Col>
              ))}
            </Row>
          </div>
        </Spin>
      </div>
    </div>
  );
};

export default HomePage;
