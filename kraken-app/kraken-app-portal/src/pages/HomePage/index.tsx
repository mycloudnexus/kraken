import { PageLayout } from "@/components/Layout";
import { useGetProductEnvs } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import ActivityDiagrams from "./components/ActivityDiagrams";
import QuickStartGuide from "./components/QuickStartGuide";

const HomePage = () => {
  const { currentProduct } = useAppStore();
  const { data: envs } = useGetProductEnvs(currentProduct);

  return (
    <PageLayout title="" style={{ background: "#F0F2F5", paddingTop: "15px" }}>
      <QuickStartGuide />
      {envs && <ActivityDiagrams envs={envs.data} />}
    </PageLayout>
  );
};

export default HomePage;
