import { useGetDataPlaneApiUseCases } from "@/hooks/mappingTemplate";
import { useAppStore } from "@/stores/app.store";
import { ApiList } from "./components/ApiList";
import { TransferIcon } from "./components/TransferIcon";
import { useMemo } from "react";
import { IRunningMapping } from "@/utils/types/env.type";
import { useNavigate } from "react-router-dom";
import { Tooltip } from "antd";
import { InfoCircleOutlined } from "@ant-design/icons";

export default function ProductionUpgrade({
  stageEnvId,
  productEnvId,
  isUpgrading,
  isUpgraded,
  upgradeVersion,
}: Readonly<{
  stageEnvId?: string;
  productEnvId?: string;
  isUpgrading?: boolean;
  isUpgraded?: boolean;
  upgradeVersion?: string;
}>) {
  const navigate = useNavigate();
  const { currentProduct: productId } = useAppStore();

  const { data: stageUseCases, isFetching: isFetchingStageUpgrade } =
    useGetDataPlaneApiUseCases(productId, stageEnvId as any);
  const {
    data: productionUseCases,
    isFetching: isFetchingProductionUpgrade,
  } = useGetDataPlaneApiUseCases(productId, productEnvId as any);

  const handleItemClick = (item: IRunningMapping) => {
    navigate(
      `/api-mapping/${item.componentKey}?targetMapperKey=${item.targetMapperKey}&version=${upgradeVersion}`
    );
  };

  // Updated: every stage upgradeable use cases should be highlighted, even though are completed or not
  const upgradeableStageMapperKeys = useMemo(() => {
    if (stageUseCases && productionUseCases) {
      const stageMapperKeyUseCases = new Set(productionUseCases.map(usecase => usecase.targetMapperKey))

      return stageUseCases.reduce((acc: Record<string, boolean>, usecase) => {
        if (stageMapperKeyUseCases.has(usecase.targetMapperKey)) {
          acc[usecase.targetMapperKey] = true
        }

        return acc
      }, {})
    }

    return {}
  }, [stageUseCases, productionUseCases])

  return (
    <>
      <ApiList
        title={
          <>
            Data plane (stage) API mappings ({stageUseCases?.length ?? 0})
            <Tooltip title="Use cases highlighted are to be upgraded to Production data plane.">
              <InfoCircleOutlined
                style={{ marginLeft: 8, color: "var(--text-secondary)" }}
              />
            </Tooltip>
          </>}
        loading={isFetchingStageUpgrade}
        details={stageUseCases}
        clickable
        highlights={upgradeableStageMapperKeys} // targetMapperKeys
        indicators={['incomplete']}
        onItemClick={handleItemClick}
        statusIndicatorPosition="right"
      />

      <TransferIcon active={isUpgrading} completed={isUpgraded} />

      <ApiList
        title={`Data plane (production) API mappings (${productionUseCases?.length ?? 0})`}
        loading={isFetchingProductionUpgrade}
        details={productionUseCases}
      />
    </>
  );
}
