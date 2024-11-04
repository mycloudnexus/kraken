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

export default function ControlPlaneUpgrade({
  isUpgrading,
  isUpgraded,
}: Readonly<{ isUpgrading?: boolean; isUpgraded?: boolean }>) {
  const { templateUpgradeId } = useParams<{ templateUpgradeId: string }>();
  const { currentProduct: productId } = useAppStore();

  const {
    data: templateUpgradeApiUseCases,
    isFetching: isFetchingTemplateUpgradeApis,
  } = useGetListTemplateUpgradeApiUseCases(
    productId,
    templateUpgradeId as string
  );
  const { data: apiUseCases, isFetching: isFetchingApiUseCases } =
    useGetListApiUseCases(productId);

  const leftApis: IRunningMapping[] | undefined = useMemo(
    () => templateUpgradeApiUseCases?.flatMap(({ details }) => details),
    [templateUpgradeApiUseCases]
  );
  const rightApis: IRunningMapping[] | undefined = useMemo(
    () => apiUseCases?.flatMap(({ details }) => details),
    [apiUseCases]
  );

  return (
    <>
      <ApiList
        title={`New template API mappings (${leftApis?.length ?? 0})`}
        loading={isFetchingTemplateUpgradeApis}
        details={leftApis}
        statusIndicatorPosition="right"
      />

      <TransferIcon active={isUpgrading} completed={isUpgraded} />

      <ApiList
        title={`Control plane API mappings (${rightApis?.length ?? 0})`}
        loading={isFetchingApiUseCases}
        details={rightApis}
        statusIndicatorPosition="left"
      />
    </>
  );
}
