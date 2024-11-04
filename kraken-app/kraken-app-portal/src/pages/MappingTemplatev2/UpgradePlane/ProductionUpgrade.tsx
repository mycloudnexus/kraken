import { useGetDataPlaneApiUseCases } from "@/hooks/mappingTemplate";
import { useAppStore } from "@/stores/app.store";
import { ApiList } from "./components/ApiList";
import { TransferIcon } from "./components/TransferIcon";

export default function ProductionUpgrade({
  stageEnvId,
  productEnvId,
  isUpgrading,
  isUpgraded,
}: Readonly<{
  stageEnvId?: string;
  productEnvId?: string;
  isUpgrading?: boolean;
  isUpgraded?: boolean;
}>) {
  const { currentProduct: productId } = useAppStore();

  const { data: stageTemplateUpgrade, isFetching: isFetchingStageUpgrade } =
    useGetDataPlaneApiUseCases(productId, stageEnvId as any);
  const {
    data: productionTemplateUpgrade,
    isFetching: isFetchingProductionUpgrade,
  } = useGetDataPlaneApiUseCases(productId, productEnvId as any);

  return (
    <>
      <ApiList
        title={`Data plane (stage) API mappings (${stageTemplateUpgrade?.length ?? 0})`}
        loading={isFetchingStageUpgrade}
        details={stageTemplateUpgrade}
      />

      <TransferIcon active={isUpgrading} completed={isUpgraded} />

      <ApiList
        title={`Data plane (production) API mappings (${productionTemplateUpgrade?.length ?? 0})`}
        loading={isFetchingProductionUpgrade}
        details={productionTemplateUpgrade}
      />
    </>
  );
}
