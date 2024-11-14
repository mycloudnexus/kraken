import {
  useGetDataPlaneApiUseCases,
  useGetListApiUseCases,
} from "@/hooks/mappingTemplate";
import { useAppStore } from "@/stores/app.store";
import { IRunningMapping } from "@/utils/types/env.type";
import { InfoCircleOutlined } from "@ant-design/icons";
import { Tooltip } from "antd";
import { useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { useMappingTemplateStoreV2 } from "../store";
import { ApiList } from "./components/ApiList";
import { TransferIcon } from "./components/TransferIcon";

export default function StageUpgrade({
  stageEnvId,
  upgradeVersion,
  isUpgrading,
  isUpgraded,
}: Readonly<{
  stageEnvId?: string;
  isUpgrading?: boolean;
  upgradeVersion?: string;
  isUpgraded?: boolean;
}>) {
  const navigate = useNavigate();
  const { currentProduct: productId } = useAppStore();

  const {
    data: controlPlaneApiUseCases,
    isFetching: isFetchingControlPlaneApis,
  } = useGetListApiUseCases(productId);
  const { data: stageApiUseCases, isFetching: isFetchingStageApis } =
    useGetDataPlaneApiUseCases(productId, stageEnvId as string);

  const { isMappingIncomplete, pushNotification, setIsMappingIncomplete } =
    useMappingTemplateStoreV2();

  const handleItemClick = (item: IRunningMapping) => {
    navigate(
      `/api-mapping/${item.componentKey}?targetMapperKey=${item.targetMapperKey}&version=${upgradeVersion}`
    );
  };

  const controlPlaneApis: IRunningMapping[] | undefined = useMemo(
    () =>
      controlPlaneApiUseCases?.flatMap(({ details, componentKey }) =>
        details.map((detail) => ({
          ...detail,
          componentKey,
        }))
      ),
    [controlPlaneApiUseCases]
  );

  // Updated: every control plane upgradeable use cases should be highlighted, even though are completed or not
  const upgradeableControlPlaneMapperKeys = useMemo(() => {
    if (controlPlaneApis && stageApiUseCases) {
      const stageMapperKeyUseCases = new Set(stageApiUseCases.map(usecase => usecase.targetMapperKey))

      return controlPlaneApis.reduce((acc: Record<string, boolean>, usecase) => {
        if (stageMapperKeyUseCases.has(usecase.targetMapperKey)) {
          acc[usecase.targetMapperKey] = true
        }

        return acc
      }, {})
    }

    return {}
  }, [controlPlaneApis, stageApiUseCases])

  useEffect(() => {
    // Check for any incomplete mapping use cases derived from control plane upgrade
    if (
      controlPlaneApis &&
      !isMappingIncomplete &&
      controlPlaneApis.some((usecase) => upgradeableControlPlaneMapperKeys?.[usecase.targetMapperKey] && usecase.mappingStatus === "incomplete")
    ) {
      pushNotification({
        type: "warning",
        message:
          "Please adjust and complete the incomplete mapping use cases that will be upgraded to data plane.",
      });

      setIsMappingIncomplete(true);
    }
  }, [controlPlaneApis, upgradeableControlPlaneMapperKeys]);

  return (
    <>
      <ApiList
        title={
          <>
            Control plane API mappings ({controlPlaneApis?.length ?? 0})
            <Tooltip title="Use cases highlighted are to be upgraded to Stage data plane. Please ensure all of their mapping are correct and complete.">
              <InfoCircleOutlined
                style={{ marginLeft: 8, color: "var(--text-secondary)" }}
              />
            </Tooltip>
          </>
        }
        loading={isFetchingControlPlaneApis}
        details={controlPlaneApis}
        statusIndicatorPosition="right"
        clickable
        highlights={upgradeableControlPlaneMapperKeys} // targetMapperKeys
        indicators={['incomplete']}
        upgradeable={upgradeableControlPlaneMapperKeys}
        onItemClick={handleItemClick}
      />

      <TransferIcon active={isUpgrading} completed={isUpgraded} />

      <ApiList
        title={`Data plane: Stage API mappings (${stageApiUseCases?.length ?? 0})`}
        loading={isFetchingStageApis}
        details={stageApiUseCases}
      />
    </>
  );
}
