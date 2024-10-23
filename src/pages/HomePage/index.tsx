import { PageLayout } from "@/components/Layout";
import { useGetComponentList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { COMPONENT_KIND_API_TARGET_SPEC } from "@/utils/constants/product";
import { clearData, getData } from "@/utils/helpers/token";
import { isEmpty } from "lodash";
import { useCallback, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import StepCard from "./components/StepCard";

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
    <PageLayout title="MEF LSO Sonata Adapters">
      <StepCard
        navigateApi={navigateApi}
        navigateCreateAPI={navigateCreateAPI}
      />
    </PageLayout>
  );
};

export default HomePage;
