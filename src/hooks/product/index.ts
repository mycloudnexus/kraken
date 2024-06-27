import {
  createApiKey,
  createNewComponent,
  createNewVersion,
  deployProduct,
  editComponentDetail,
  getAllApiKeyList,
  getAllDataPlaneList,
  getComponentAPIDoc,
  getComponentDetail,
  getEnvActivity,
  getListComponentVersions,
  getListComponents,
  getListDeployments,
  getListEnvActivities,
  getRunningComponentList,
  getListEnvs,
  getVersionList,
  getRunningVersionList,
  updateTargetMapper,
} from "@/services/products";
import { queryClient } from "@/utils/helpers/reactQuery";
import {
  IPagingData,
  IPagingParams,
  IUnifiedAsset,
} from "@/utils/types/common.type";
import {
  IComponentVersion,
  IProductWithComponentVersion,
} from "@/utils/types/component.type";
import {
  IActivityDetail,
  IActivityLog,
  IApiKeyDetail,
  IDataPlaneDetail,
  IEnv,
  IRunningComponentItem,
} from "@/utils/types/env.type";

import { IEnvComponent } from "@/utils/types/envComponent.type";
import { useMutation, useQuery, useQueries } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { get } from "lodash";

export const PRODUCT_CACHE_KEYS = {
  get_product_component_list: "get_product_component_list",
  get_component_api_doc: "get_component_api_doc",
  create_new_component: "create_new_component",
  get_component_list: "get_component_list",
  get_product_env_list: "get_product_env_list",
  get_product_env_activity_list: "get_product_env_activity_list",
  get_product_env_activity_detail: "get_product_env_activity_detail",
  get_component_detail: "get_component_detail",
  edit_component_detail: "edit_component_detail",
  get_product_deployment_list: "get_product_deployment_list",
  get_product_component_version_list: "get_product_component_version_list",
  deploy_product: "deploy_product",
  get_all_api_key: "get_all_api_key",
  get_all_data_plane: "get_all_data_plane",
  get_running_component: "get_running_component",
  create_new_version: "create_new_version",
  get_version_list: "get_version_list",
  create_api_key: "create_api_key",
  get_running_version: "get_running_version",
  update_target_mapper: "update_target_mapper",
};

export const useGetProductComponents = (
  productId: string,
  params: Record<string, any>
) => {
  const { data, ...result } = useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_product_component_list, params],
    queryFn: () => getListComponents(productId, params),
    enabled: Boolean(productId),
  });
  return {
    data: get(data, "data"),
    ...result,
  };
};

export const useGetComponentAPIDocs = (
  productId: string,
  componentId: string
) => {
  return useQuery<any, Error>({
    queryKey: [
      PRODUCT_CACHE_KEYS.get_component_api_doc,
      productId,
      componentId,
    ],
    queryFn: () => getComponentAPIDoc(productId, componentId),
    enabled: Boolean(productId && componentId),
  });
};

export const useCreateNewComponent = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.create_new_component],
    mutationFn: ({ productId, data }: any) =>
      createNewComponent(productId, data),
  });
};

export const useGetComponentList = (
  productId: string,
  params: Record<string, any>
) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_component_list, productId, params],
    queryFn: () => getListComponents(productId, params),
    enabled: Boolean(productId),
    select: (data) => data?.data,
  });
};

export const useManualGetComponentList = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.get_component_list],
    mutationFn: ({ productId, params }: any) =>
      getListComponents(productId, params),
  });
};

export const useGetProductEnvs = (productId: string, enabled = true) => {
  return useQuery<AxiosResponse, Error, IPagingData<IEnv>>({
    queryKey: [PRODUCT_CACHE_KEYS.get_product_env_list, productId],
    queryFn: () => getListEnvs(productId),
    enabled: enabled && Boolean(productId),
    select: (data) => data.data,
  });
};

export const useGetProductEnvActivities = (
  productId: string,
  envId: string,
  params: unknown
) => {
  return useQuery<AxiosResponse, Error, IPagingData<IActivityLog>>({
    queryKey: [
      PRODUCT_CACHE_KEYS.get_product_env_activity_list,
      productId,
      envId,
      params,
    ],
    queryFn: () => getListEnvActivities(productId, envId, params),
    enabled: Boolean(productId && envId),
    select: (data) => data.data,
  });
};

export const useGetProductEnvActivityDetail = (
  productId: string,
  envId: string,
  activityId: string
) => {
  return useQuery<any, Error, IActivityDetail>({
    queryKey: [
      PRODUCT_CACHE_KEYS.get_product_env_activity_detail,
      productId,
      envId,
      activityId,
    ],
    queryFn: () => getEnvActivity(productId, envId, activityId),
    enabled: Boolean(productId && envId && activityId),
    select: (data) => data.data,
  });
};

export const useGetComponentDetail = (
  productId: string,
  componentId: string,
  open = true
) => {
  return useQuery<any, Error, IUnifiedAsset>({
    queryKey: [PRODUCT_CACHE_KEYS.get_component_detail, productId, componentId],
    queryFn: () => getComponentDetail(productId, componentId),
    enabled: Boolean(productId && componentId && open),
    select: (data) => data.data,
  });
};

export const useEditComponent = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.edit_component_detail],
    mutationFn: ({ productId, componentId, data }: any) =>
      editComponentDetail(productId, componentId, data),
  });
};

export const useGetProductDeployments = (
  productId: string,
  params: unknown
) => {
  return useQuery<any, Error, IPagingData<IEnvComponent>>({
    queryKey: [
      PRODUCT_CACHE_KEYS.get_product_deployment_list,
      productId,
      params,
    ],
    queryFn: () => getListDeployments(productId, params),
    enabled: Boolean(productId),
    select: (data) => data.data,
  });
};

export const useGetProductComponentVersions = (
  productId: string,
  enabled = true
) => {
  return useQuery<any, Error, IProductWithComponentVersion[]>({
    queryKey: [
      PRODUCT_CACHE_KEYS.get_product_component_version_list,
      productId,
    ],
    queryFn: () => getListComponentVersions(productId),
    enabled: enabled && Boolean(productId),
    select: (data) => data.data,
  });
};

export const useDeployProduct = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.deploy_product],
    mutationFn: ({ productId, envId, data }: any) =>
      deployProduct(productId, envId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_product_deployment_list],
      });
    },
  });
};

export const useGetAllApiKeyList = (
  productId: string,
  params: IPagingParams,
  enabled = true
) => {
  return useQuery<AxiosResponse, Error, IPagingData<IApiKeyDetail>>({
    queryKey: [PRODUCT_CACHE_KEYS.get_all_api_key, productId],
    queryFn: () => getAllApiKeyList(productId, params),
    enabled: enabled && Boolean(productId),
    select: (data) => data.data,
  });
};

export const useCreateApiKey = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.create_api_key],
    mutationFn: (data: any): any => createApiKey(data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_all_api_key],
      });
    },
  });
};

export const useGetAllDataPlaneList = (
  productId: string,
  params: IPagingParams,
  enabled = true
) => {
  return useQuery<AxiosResponse, Error, IPagingData<IDataPlaneDetail>>({
    queryKey: [PRODUCT_CACHE_KEYS.get_all_data_plane, productId],
    queryFn: () => getAllDataPlaneList(productId, params),
    enabled: enabled && Boolean(productId),
    select: (data) => data.data,
  });
};
export const useGetRunningComponentList = (
  productId: string,
  enabled = true
) => {
  return useQuery<AxiosResponse, Error, IPagingData<IRunningComponentItem>>({
    queryKey: [PRODUCT_CACHE_KEYS.get_running_component, productId],
    queryFn: () => getRunningComponentList(productId),
    enabled: enabled && Boolean(productId),
    select: (data) => data.data,
  });
};
export const useCreateNewVersion = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.create_new_version],
    mutationFn: (data: any) => createNewVersion(data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_version_list],
      });
    },
  });
};

export const useGetVersionList = (
  productId: string,
  componentId: string,
  params: Record<string, any>
) => {
  return useQuery<any, Error, IPagingData<IComponentVersion>>({
    queryKey: [
      PRODUCT_CACHE_KEYS.get_version_list,
      productId,
      componentId,
      params,
    ],
    queryFn: () => getVersionList(productId, componentId, params),
    enabled: Boolean(productId && componentId),
    select: (data) => data.data,
  });
};

export const useGetRunningVersion = (
  productId: string,
  componentId: string
) => {
  return useQuery<any, Error, IPagingData<IComponentVersion>>({
    queryKey: [PRODUCT_CACHE_KEYS.get_running_version, productId, componentId],
    queryFn: () => getRunningVersionList(productId, componentId),
    enabled: Boolean(productId && componentId),
    select: (data) => data.data,
  });
};

export const useGetRunningVersionList = (params: any) => {
  const { componentIds = [], productId } = params;
  return useQueries({
    queries: componentIds.map((id: string) => ({
      queryKey: [PRODUCT_CACHE_KEYS.get_running_version, id],
      queryFn: () => getRunningVersionList(productId, id),
      enabled: Boolean(productId && id),
      select: (data: any) => data.data,
    })),
  });
};

export const useUpdateTargetMapper = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.update_target_mapper],
    mutationFn: ({ productId, componentId, data }: any) =>
      updateTargetMapper(productId, componentId, data),
  });
};
