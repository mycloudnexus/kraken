import {
  useGetListApiUseCases,
  useGetListTemplateUpgradeApiUseCases,
} from "@/hooks/mappingTemplate";
import { useAppStore } from "@/stores/app.store";
import { IRunningMapping } from "@/utils/types/env.type";
import { useMemo } from "react";
import { useParams } from "react-router-dom";
import { ApiList } from "./components/ApiList";
import { TransferIcon } from "./components/TransferIcon";
import { useLongPolling } from "@/hooks/useLongPolling";
import { LONG_POLLING_TIME } from "../utils";

export default function ControlPlaneUpgrade({
  isUpgrading,
  isUpgraded,
}: Readonly<{ isUpgrading?: boolean; isUpgraded?: boolean }>) {
  const { templateUpgradeId } = useParams<{ templateUpgradeId: string }>();
  const { currentProduct: productId } = useAppStore();

  const {
    data: templateUpgradeApiUseCases,
    isLoading: isLoadingTemplateUpgradeApis,
  } = useGetListTemplateUpgradeApiUseCases(
    productId,
    templateUpgradeId as string
  );
  const { data: controlPlaneApis, isLoading: isLoadingControlPlaneApis } =
    useLongPolling(
      useGetListApiUseCases(productId),
      LONG_POLLING_TIME,
      {
        active: isUpgrading,
      }
    );

  const leftApis: IRunningMapping[] | undefined = useMemo(
    () => templateUpgradeApiUseCases?.flatMap(({ details }) => details),
    [templateUpgradeApiUseCases]
  );
  const rightApis: IRunningMapping[] | undefined = useMemo(
    () => controlPlaneApis?.flatMap(({ details }) => details),
    [controlPlaneApis]
  );

  return (
    <>
      <ApiList
        title={`New template API mappings (${leftApis?.length ?? 0})`}
        loading={isLoadingTemplateUpgradeApis}
        details={leftApis}
      />

      <TransferIcon active={isUpgrading} completed={isUpgraded} />

      <ApiList
        title={`Control plane API mappings (${rightApis?.length ?? 0})`}
        loading={isLoadingControlPlaneApis}
        details={rightApis}
        indicators={['incomplete']}
        statusIndicatorPosition="left"
      />
    </>
  );
}
