import {
  checkStageUpgradeCheck,
  controlPlaneTemplateUpgrade,
  getListApiUseCase,
  getListDataPlaneUseCase,
  getListTemplateUpgradeApiUseCase,
  getMappingTemplateReleaseHistory,
  getTemplateMappingReleaseDetail,
  productionTemplateUpgrade,
  stageTemplateUpgrade,
} from "@/services/mappingTemplate";
import { STALE_TIME } from "@/utils/constants/common";
import { queryClient } from "@/utils/helpers/reactQuery";
import { IPagingData } from "@/utils/types/common.type";
import { IRunningMapping } from "@/utils/types/env.type";
import { IApiUseCase, IReleaseHistory } from "@/utils/types/product.type";
import {
  InfiniteData,
  useInfiniteQuery,
  useMutation,
  useQuery,
} from "@tanstack/react-query";
import { AxiosResponse } from "axios";

// Mapping template query keys
export enum MTQueryKey {
  LIST_TEMPLATE_RELEASE_HISTORY = "list_mapping_template_release_history",
  GET_TEMPLATE_RELEASE_HISTORY_DETAIL = "get_mapping_template_release_history_detail",
  GET_TEMPLATE_RELEASE_DETAIL = "get_template_release_detail",
  LIST_TEMPLATE_UPGRADE_API_USE_CASE = "get_list_template_upgrade_api_use_case",
  LIST_API_USE_CASE = "get_list_api_use_case",
  LIST_DATA_PLANE_API_USE_CASE = "get_list_data_plane_api_use_case",
  CONTROL_PLANE_TEMPLATE_UPGRADE = "control_plane_template_upgrade",
  STAGE_TEMPLATE_UPGRADE = "stage_template_upgrade",
  PRODUCTION_TEMPLATE_UPGRADE = "production_template_upgrade",
  CHECK_STAGE_PRE_UPGRADE = "check_stage_pre_upgrade",
  CHECK_PRODUCTION_PRE_UPGRADE = "check_production_pre_upgrade",
}

export function useInfiniteReleaseHistoryQuery(
  productId: string,
  params: Record<string, any>
) {
  return useInfiniteQuery<
    any,
    Error,
    InfiniteData<{ data: IPagingData<IReleaseHistory> }>
  >({
    queryKey: [MTQueryKey.LIST_TEMPLATE_RELEASE_HISTORY, productId, params],
    queryFn: ({ pageParam }) =>
      getMappingTemplateReleaseHistory(productId, {
        ...params,
        page: pageParam,
      }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      const { page, total, size } = lastPage.data as Omit<
        IPagingData<any>,
        "data"
      >;

      if (page * size >= total) {
        return undefined;
      }
      return page + 1;
    },
    enabled: Boolean(productId),
    staleTime: STALE_TIME,
  });
}

export function useGetTemplateMappingReleaseDetail(
  productId: string,
  templateUpgradeId: string
) {
  return useQuery<AxiosResponse, Error, IReleaseHistory>({
    queryKey: [
      MTQueryKey.GET_TEMPLATE_RELEASE_HISTORY_DETAIL,
      productId,
      templateUpgradeId,
    ],
    queryFn: () =>
      getTemplateMappingReleaseDetail(productId, templateUpgradeId),
    enabled: Boolean(templateUpgradeId),
    staleTime: STALE_TIME,
    select: (data) => data.data?.data?.[0],
  });
}

// This serves upgrade screen's left side api use cases panel
export function useGetListTemplateUpgradeApiUseCases(
  productId: string,
  templateUpgradeId: string
) {
  return useQuery<AxiosResponse, Error, IApiUseCase[]>({
    queryKey: [
      MTQueryKey.LIST_TEMPLATE_UPGRADE_API_USE_CASE,
      productId,
      templateUpgradeId,
    ],
    queryFn: () =>
      getListTemplateUpgradeApiUseCase(productId, templateUpgradeId),
    staleTime: STALE_TIME,
    enabled: Boolean(productId) && Boolean(templateUpgradeId),
    select: (data) => data.data,
  });
}

// This serves upgrade screen's right side api use cases panel
export function useGetListApiUseCases(productId: string) {
  return useQuery<AxiosResponse, Error, IApiUseCase[]>({
    queryKey: [MTQueryKey.LIST_API_USE_CASE, productId],
    queryFn: () => getListApiUseCase(productId),
    staleTime: STALE_TIME,
    enabled: Boolean(productId),
    select: (data) => data.data,
  });
}

// envId = stageEnvId or productionEnvId
export function useGetDataPlaneApiUseCases(productId: string, envId: string) {
  return useQuery<AxiosResponse, Error, IRunningMapping[]>({
    queryKey: [MTQueryKey.LIST_DATA_PLANE_API_USE_CASE, productId, envId],
    queryFn: () => getListDataPlaneUseCase(productId, envId),
    staleTime: STALE_TIME,
    enabled: Boolean(productId) && Boolean(envId),
    select: (data) => data.data,
  });
}

export function useControlPlaneTemplateUpgrade(
  productId: string,
  {
    onError,
    onSuccess,
  }: { onSuccess(message: string): void; onError(message: string): void }
) {
  return useMutation<AxiosResponse, Error, { templateUpgradeId: string }>({
    mutationKey: [MTQueryKey.CONTROL_PLANE_TEMPLATE_UPGRADE, productId],
    mutationFn: (data) => controlPlaneTemplateUpgrade(productId, data),
    onSuccess() {
      onSuccess("Control plane upgrade successfully");
      queryClient.invalidateQueries({
        queryKey: [
          MTQueryKey.LIST_TEMPLATE_UPGRADE_API_USE_CASE,
          MTQueryKey.LIST_API_USE_CASE,
          productId,
        ],
      });
    },
    onError(error: any) {
      onError(error.reason);
    },
  });
}

export function useStageTemplateUpgrade(
  productId: string,
  {
    onError,
    onSuccess,
  }: { onSuccess(message: string): void; onError(message: string): void }
) {
  return useMutation<
    AxiosResponse,
    Error,
    { templateUpgradeId: string; stageEnvId: string }
  >({
    mutationKey: [MTQueryKey.STAGE_TEMPLATE_UPGRADE, productId],
    mutationFn: (data) => stageTemplateUpgrade(productId, data),
    onSuccess() {
      onSuccess(
        "Mapping template upgrade successfully and effective now in stage data plane. Please test offline and ensure they can work properly."
      );
    },
    onError(error: any) {
      onError(error.reason);
    },
  });
}

export function useProductionTemplateUpgrade(
  productId: string,
  {
    onError,
    onSuccess,
  }: { onSuccess(message: string): void; onError(message: string): void }
) {
  return useMutation<
    AxiosResponse,
    Error,
    { templateUpgradeId: string; stageEnvId: string; productionEnvId: string }
  >({
    mutationKey: [MTQueryKey.PRODUCTION_TEMPLATE_UPGRADE, productId],
    mutationFn: (data) => productionTemplateUpgrade(productId, data),
    onSuccess() {
      onSuccess(
        "Mapping template upgrade successfully and effective now in production data plane. "
      );
    },
    onError(error: any) {
      onError(error.reason);
    },
  });
}

// pre-upgrade checking
export function useStageUpgradeCheck(
  productId: string,
  templateUpgradeId: string,
  envId: string
) {
  return useQuery<AxiosResponse, Error, IRunningMapping[]>({
    queryKey: [
      MTQueryKey.CHECK_STAGE_PRE_UPGRADE,
      productId,
      templateUpgradeId,
      envId,
    ],
    queryFn: () => checkStageUpgradeCheck(productId, templateUpgradeId, envId),
    staleTime: STALE_TIME,
    enabled: Boolean(productId) && Boolean(templateUpgradeId) && Boolean(envId),
    select: (data) => data.data,
  });
}

export function useProductionUpgradeCheck(
  productId: string,
  templateUpgradeId: string,
  envId: string
) {
  return useQuery<AxiosResponse, Error, IRunningMapping[]>({
    queryKey: [
      MTQueryKey.CHECK_PRODUCTION_PRE_UPGRADE,
      productId,
      templateUpgradeId,
      envId,
    ],
    queryFn: () => checkStageUpgradeCheck(productId, templateUpgradeId, envId),
    staleTime: STALE_TIME,
    enabled: Boolean(productId) && Boolean(templateUpgradeId) && Boolean(envId),
    select: (data) => data.data,
  });
}
