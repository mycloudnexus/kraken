import styles from "./index.module.scss";
import { Col, Flex, Row, Spin } from "antd";
import { useNavigate } from "react-router-dom";
import { useAppStore } from "@/stores/app.store";
import { MoreIcon } from "./components/Icon";
import { COMPONENT_KIND_API_TARGET_SPEC } from "@/utils/constants/product";
import StepCard from "./components/StepCard";
import ApiComponents from "./components/ApiComponents";
import { useGetComponentList } from "@/hooks/product";
import { useCallback } from "react";
import { isEmpty } from "lodash";

const HomePage = () => {
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();
  const { data: apiList, isLoading } = useGetComponentList(currentProduct, {
    kind: COMPONENT_KIND_API_TARGET_SPEC,
    size: 10000,
    page: 0,
  });

  const navigateApi = useCallback(() => {
    if (isEmpty(apiList?.data)) {
      navigate(`/component/${currentProduct}/new`);
      return;
    }
    navigate(`/component/${currentProduct}/list`);
  }, [apiList, currentProduct]);

  return (
    <div className={styles.homePage}>
      <h1>MEF LSO Sonata Adapters</h1>
      <StepCard navigateApi={navigateApi} />
      <ApiComponents />
      <Flex gap={36}>
        <Row
          justify={"space-between"}
          className={styles.bottomItem}
          onClick={() => navigate(`/env`)}
        >
          <Col>Environments overview </Col>
          <Col>
            <MoreIcon />
          </Col>
        </Row>

        <Row
          justify={"space-between"}
          className={styles.bottomItem}
          onClick={navigateApi}
        >
          <Col>
            <Spin spinning={isLoading} style={{ flex: 1 }}>
              Seller APIs {`(${apiList?.total ?? 0})`}
            </Spin>
          </Col>
          <Col>
            <MoreIcon />
          </Col>
        </Row>
      </Flex>
    </div>
  );
};

export default HomePage;
