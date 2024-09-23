import {
  createApiKey,
  createNewComponent,
  createNewVersion,
  deployProduct,
  editComponentDetail,
  getAllApiKeyList,
  getAllDataPlaneList,
  getComponentDetail,
  getComponentDetailMapping,
  getEnvActivity,
  getListComponentVersions,
  getListComponents,
  getListComponentsV2,
  getListDeployments,
  getListEnvActivities,
  getRunningComponentList,
  getListEnvs,
  getVersionList,
  getRunningVersionList,
  updateTargetMapper,
  getMapperDetails,
  deployToEnv,
  getRunningAPIMappingList,
  getAPIMapperDeployments,
  getBuyerList,
  createBuyer,
  verifyProduct,
  deployProduction,
  getAuditLogs,
  getLatestRunningAPI,
  getMappingTemplateReleaseHistory,
  getMappingTemplateCurrentVersion,
  getMappingTemplateUpgradeList,
  getMappingTemplateUpgradeDetail,
  productUpgradeStage,
  productUpgradeProd,
} from "@/services/products";
import {
  COMPONENT_KIND_API,
  COMPONENT_KIND_API_SPEC,
  COMPONENT_KIND_API_TARGET_SPEC,
} from "@/utils/constants/product";
import { queryClient } from "@/utils/helpers/reactQuery";
import {
  IDetailsData,
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
  IMapperDetails,
  IRunningComponentItem,
} from "@/utils/types/env.type";

import { IEnvComponent } from "@/utils/types/envComponent.type";
import { useMutation, useQuery, useQueries } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { get } from "lodash";

export const PRODUCT_CACHE_KEYS = {
  get_audit_logs: "get_audit_logs",
  get_product_component_list: "get_product_component_list",
  get_component_api_doc: "get_component_api_doc",
  create_new_component: "create_new_component",
  get_component_list: "get_component_list",
  get_component_list_v2: "get_component_list_v2",
  get_product_env_list: "get_product_env_list",
  get_product_env_activity_list: "get_product_env_activity_list",
  get_product_env_activity_detail: "get_product_env_activity_detail",
  get_component_detail_mapping: "get_component_detail_mapping",
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
  get_mapper_details: "get_mapper_details",
  deploy_to_env: "deploy_to_env",
  get_running_api_list: "get_running_api_list",
  get_list_api_deployments: "get_list_api_deployments",
  get_buyer_list: "get_buyer_list",
  create_buyer: "create_buyer",
  verify_product: "verify_product",
  deploy_stage_to_production: "deploy_stage_to_production",
  get_component_list_spec: "get_component_list_spec",
  get_component_list_api_spec: "get_component_list_api_spec",
  get_component_list_api: "get_component_list_api",
  get_running_list_api: "get_running_list_api",
  get_release_list: "get_release_list",
  get_current_version: "get_current_version",
  get_upgrade_list: "get_upgrade_list",
  get_upgrade_detail: "get_upgrade_detail",
  upgrade_mapping_template_stage: "upgrade_mapping_template_stage",
  upgrade_mapping_template_production: "upgrade_mapping_template_production",
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

export const useGetComponentListAPI = (productId: string) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_component_list_api, productId],
    queryFn: () =>
      getListComponents(productId, {
        kind: COMPONENT_KIND_API,
        size: 500,
      }),
    enabled: Boolean(productId),
    select: (data) => data?.data,
  });
};

export const useGetComponentListSpec = (productId: string) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_component_list_spec, productId],
    queryFn: () =>
      getListComponents(productId, {
        kind: COMPONENT_KIND_API_SPEC,
        size: 500,
      }),
    enabled: Boolean(productId),
    select: (data) => data?.data,
  });
};

export const useGetComponentListAPISpec = (productId: string) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_component_list_api_spec, productId],
    queryFn: () =>
      getListComponents(productId, {
        kind: COMPONENT_KIND_API_TARGET_SPEC,
        size: 500,
      }),
    enabled: Boolean(productId),
    select: (data) => data?.data,
  });
};

export const useGetComponentListV2 = (
  productId: string,
  targetMapperKey: string
) => {
  return useQuery<any, Error>({
    queryKey: [
      PRODUCT_CACHE_KEYS.get_component_list_v2,
      productId,
      targetMapperKey,
    ],
    queryFn: () => getListComponentsV2(productId, targetMapperKey),
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

export const useGetComponentDetailMapping = (
  productId: string,
  componentId: string,
  open = true
) => {
  return useQuery<any, Error, IDetailsData<IMapperDetails>>({
    queryKey: [
      PRODUCT_CACHE_KEYS.get_component_detail_mapping,
      productId,
      componentId,
    ],
    queryFn: () => getComponentDetailMapping(productId, componentId),
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

export const useGetMapperDetails = (productId: string, componentId: string) => {
  return useQuery<any, Error, any>({
    queryKey: [PRODUCT_CACHE_KEYS.get_mapper_details, productId, componentId],
    queryFn: () => getMapperDetails(productId, componentId),
    enabled: Boolean(productId && componentId),
    select: (data) => get(data, "data.details"),
  });
};

export const useDeployToEnv = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.deploy_to_env],
    mutationFn: ({ productId, componentId, mapperKeys, envId }: any) =>
      deployToEnv(productId, componentId, mapperKeys, envId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_running_api_list],
      });
    },
  });
};

export const useGetRunningAPIList = (
  productId: string,
  params: Record<string, any>
) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_running_api_list, productId, params],
    queryFn: () => getRunningAPIMappingList(productId, params),
    enabled: Boolean(productId) && Boolean(params.envId),
    select: (data) => data?.data,
  });
};

export const useGetAPIDeployments = (
  productId: string,
  params: Record<string, any>
) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_list_api_deployments, productId, params],
    queryFn: () => getAPIMapperDeployments(productId, params),
    enabled: Boolean(productId),
    select: (data) => data?.data,
  });
};

export const useGetAuditLogs = (params: Record<string, any>) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_audit_logs, params],
    queryFn: () => getAuditLogs(params),
    enabled: Boolean(params),
    select: (data) => data?.data,
  });
};

export const useGetBuyerList = (
  productId: string,
  params: Record<string, any>
) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_buyer_list, productId, params],
    queryFn: () => getBuyerList(productId, params),
    enabled: Boolean(productId) && Boolean(params.envId),
    select: (data) => data?.data,
  });
};

export const useCreateBuyer = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.create_buyer],
    mutationFn: ({ productId, data }: any) => createBuyer(productId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_buyer_list],
      });
    },
  });
};

export const useVerifyProduct = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.verify_product],
    mutationFn: ({ productId, data }: any) => verifyProduct(productId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_list_api_deployments],
      });
    },
  });
};

export const useDeployProduction = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.deploy_stage_to_production],
    mutationFn: ({ productId, data }: any) => deployProduction(productId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_list_api_deployments],
      });
    },
  });
};

export const useGetLatestRunningList = (
  productId: string,
  mapperKey: string
) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_running_api_list, productId, mapperKey],
    queryFn: () => getLatestRunningAPI(productId, mapperKey),
    enabled: Boolean(productId) && Boolean(mapperKey),
    select: (data) => data?.data,
  });
};

export const useGetMappingTemplateReleaseHistoryList = (
  productId: string,
  params: Record<string, any>
) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_release_list, productId, params],
    queryFn: () => getMappingTemplateReleaseHistory(productId, params),
    enabled: Boolean(productId),
    select: (data) => data?.data,
  });
};

export const useGetMappingTemplateCurrentVersion = (productId: string) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_current_version, productId],
    queryFn: () => getMappingTemplateCurrentVersion(productId),
    enabled: Boolean(productId),
    select: (data) => data?.data,
  });
};

export const useGetMappingTemplateUpgradeList = (
  productId: string,
  params: Record<string, any>
) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_upgrade_list, productId, params],
    queryFn: () => getMappingTemplateUpgradeList(productId, params),
    enabled: Boolean(productId),
    select: (data) => data?.data,
  });
};

export const useGetMappingTemplateUpgradeDetail = (
  productId: string,
  id: string
) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_upgrade_detail, productId, id],
    queryFn: () => getMappingTemplateUpgradeDetail(productId, id),
    enabled: Boolean(productId) && Boolean(id),
    select: (data) => data?.data,
  });
};

export const useDeployMappingTemplateStage = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.upgrade_mapping_template_stage],
    mutationFn: ({ productId, data }: any) =>
      productUpgradeStage(productId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_upgrade_list],
      });
    },
  });
};

export const useDeployMappingTemplateProduction = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.upgrade_mapping_template_production],
    mutationFn: ({ productId, data }: any) =>
      productUpgradeProd(productId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_upgrade_list],
      });
    },
  });
};
