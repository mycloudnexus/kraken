import styles from "./index.module.scss";
import {  Flex } from "antd";
import { useNavigate } from "react-router-dom";
import { useAppStore } from "@/stores/app.store";
import { COMPONENT_KIND_API_TARGET_SPEC } from "@/utils/constants/product";
import StepCard from "./components/StepCard";
import { useGetComponentList } from "@/hooks/product";
import { useCallback, useEffect } from "react";
import { isEmpty } from "lodash";
import { clearData, getData } from "@/utils/helpers/token";
import Text from "@/components/Text";

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


  const navigateCreateAPI = useCallback(() => {
    navigate(`/component/${currentProduct}/new`);
  }, [currentProduct]);

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
        <Text.Custom size="20px" bold="500" lineHeight="28px">
          MEF LSO Sonata Adapters
        </Text.Custom>
      </Flex>
      <StepCard navigateApi={navigateApi} navigateCreateAPI={navigateCreateAPI} />
    </div>
  );
};

export default HomePage;
