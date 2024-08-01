import styles from "./index.module.scss";
import { Button, Col, Flex, Row, Spin } from "antd";
import { useNavigate } from "react-router-dom";
import { useAppStore } from "@/stores/app.store";
import { MoreIcon } from "./components/Icon";
import { COMPONENT_KIND_API_TARGET_SPEC } from "@/utils/constants/product";
import StepCard from "./components/StepCard";
import ApiComponents from "./components/ApiComponents";
import { useGetComponentList } from "@/hooks/product";
import { useCallback, useEffect } from "react";
import { isEmpty } from "lodash";
import { UsergroupAddOutlined } from "@ant-design/icons";
import { clearData, getData } from '@/utils/helpers/token';

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

  const navigateBuy = useCallback(() => {
    navigate("/buyer");
  }, []);

  useEffect(() => {
    if (isLoading) return;

    const pathToRedirect = getData("lastVisitedPath");
    if (pathToRedirect) {
      navigate(pathToRedirect);
      clearData("lastVisitedPath");
    }
  }, [isLoading]);

  return (
    <div className={styles.homePage}>
      <Flex align="center" justify="space-between">
        <h1>MEF LSO Sonata Adapters</h1>
        <Flex align="center" justify="flex-end">
          <UsergroupAddOutlined />
          <Button
            type="link"
            style={{ padding: "4px 0px 4px 10px" }}
            onClick={navigateBuy}
          >
            Buyer management
          </Button>
        </Flex>
      </Flex>
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
