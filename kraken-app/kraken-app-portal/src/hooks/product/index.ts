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
  getAuditLogDetails,
  getLatestRunningAPI,
  getMappingTemplateReleaseHistory,
  getMappingTemplateCurrentVersion,
  getMappingTemplateUpgradeList,
  getMappingTemplateUpgradeDetail,
  productUpgradeStage,
  productUpgradeProd,
  activateBuyer,
  deactivateBuyer,
  regenerateBuyerAccessToken,
  getComponentSpecDetails,
  deleteAPIServer,
  getAPIServers,
  getComponentDetailV2,
  getValidateServerName,
  editContactInformation,
  getProductTypes,
  rotateApiKey,
} from "@/services/products";
import { STALE_TIME } from "@/utils/constants/common";
import {
  COMPONENT_KIND_API,
  COMPONENT_KIND_API_SPEC,
  COMPONENT_KIND_API_TARGET_SPEC,
  COMPONENT_KIND_SELLER_CONTACT,
} from "@/utils/constants/product";
import { queryClient } from "@/utils/helpers/reactQuery";
import {
  IDetailsData,
  IEndpointUsageAsset,
  IPagingData,
  IPagingParams,
  IUnifiedAsset,
} from "@/utils/types/common.type";
import {
  IComponent,
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
  IRunningMapping,
} from "@/utils/types/env.type";
import { IEnvComponent } from "@/utils/types/envComponent.type";
import {
  IApiMapperDeployment,
  IDeploymentHistory,
  IProductIdAndNameParams,
} from "@/utils/types/product.type";
import { useMutation, useQuery, useQueries } from "@tanstack/react-query";
import { AxiosResponse } from "axios";
import { get } from "lodash";

export const PRODUCT_CACHE_KEYS = {
  active_buyer: "active_buyer",
  create_api_key: "create_api_key",
  rotate_api_key: "rotate_api_key",
  create_buyer: "create_buyer",
  create_new_component: "create_new_component",
  create_new_version: "create_new_version",
  deactive_buyer: "deactive_buyer",
  delete_api_server: "delete_api_server",
  deploy_product: "deploy_product",
  deploy_stage_to_production: "deploy_stage_to_production",
  deploy_to_env: "deploy_to_env",
  edit_component_detail: "edit_component_detail",
  get_all_api_key: "get_all_api_key",
  get_all_data_plane: "get_all_data_plane",
  get_audit_logs: "get_audit_logs",
  get_audit_log_details: "get_audit_log_details",
  get_buyer_list: "get_buyer_list",
  get_component_api_doc: "get_component_api_doc",
  get_component_detail: "get_component_detail",
  get_component_detail_mapping: "get_component_detail_mapping",
  get_component_list: "get_component_list",
  get_seller_api_list: "get_seller_api_list",
  get_component_list_api: "get_component_list_api",
  get_component_list_api_spec: "get_component_list_api_spec",
  get_component_list_spec: "get_component_list_spec",
  get_component_list_v2: "get_component_list_v2",
  get_component_spec_details: "get_component_spec_details",
  get_current_version: "get_current_version",
  get_product_env_list: "get_product_env_list",
  get_list_api_deployments: "get_list_api_deployments",
  get_mapper_details: "get_mapper_details",
  get_product_component_list: "get_product_component_list",
  get_product_component_version_list: "get_product_component_version_list",
  get_product_deployment_list: "get_product_deployment_list",
  get_product_env_activity_detail: "get_product_env_activity_detail",
  get_product_env_activity_list: "get_product_env_activity_list",
  get_product_env_activity_list_mutation:
    "get_product_env_activity_list_mutation",
  get_product_push_history_log: "get_product_push_history_log",
  get_release_list: "get_release_list",
  get_running_api_list: "get_running_api_list",
  get_running_component: "get_running_component",
  get_running_list_api: "get_running_list_api",
  get_running_version: "get_running_version",
  get_upgrade_detail: "get_upgrade_detail",
  get_upgrade_list: "get_upgrade_list",
  get_version_list: "get_version_list",
  regenerate_token: "regenerate_token",
  update_target_mapper: "update_target_mapper",
  upgrade_mapping_template_production: "upgrade_mapping_template_production",
  upgrade_mapping_template_stage: "upgrade_mapping_template_stage",
  get_validate_api_server_name: "get_validate_api_server_name",
  verify_product: "verify_product",
  edit_contact_information: "edit_contact_information",
  get_product_type_list: "get_product_type_list",
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

export const useGetSellerAPIList = (productId: string) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_seller_api_list, productId],
    queryFn: () =>
      getAPIServers(productId, {
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

export const useGetSellerContacts = (productId: string) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_component_list_api_spec, productId],
    queryFn: () =>
      getListComponents(productId, {
        kind: COMPONENT_KIND_SELLER_CONTACT,
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
  return useQuery<any, Error, IComponent>({
    queryKey: [
      PRODUCT_CACHE_KEYS.get_component_list_v2,
      productId,
      targetMapperKey,
    ],
    queryFn: () => getListComponentsV2(productId, targetMapperKey),
    enabled: Boolean(productId && targetMapperKey),
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
    staleTime: STALE_TIME,
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

export const useGetProductEnvActivitiesMutation = () => {
  return useMutation<AxiosResponse, Error, any>({
    mutationKey: [PRODUCT_CACHE_KEYS.get_product_env_activity_list_mutation],
    mutationFn: ({ productId, envId, params }: any) =>
      getListEnvActivities(productId, envId, params),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_product_env_activity_list_mutation],
      });
    },
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

export const useGetComponentDetailV2 = (
  productId: string,
  componentId: string,
  open = true
) => {
  return useQuery<any, Error, IUnifiedAsset>({
    queryKey: [PRODUCT_CACHE_KEYS.get_component_detail, productId, componentId],
    queryFn: () => getComponentDetailV2(productId, componentId),
    enabled: Boolean(productId && componentId && open),
    select: (data) => data.data,
  });
};

export const useGetComponentSpecDetails = (
  productId: string,
  componentId: string,
  open = true
) => {
  return useQuery<any, Error, IEndpointUsageAsset>({
    queryKey: [
      PRODUCT_CACHE_KEYS.get_component_spec_details,
      productId,
      componentId,
    ],
    queryFn: () => getComponentSpecDetails(productId, componentId),
    enabled: Boolean(productId && componentId && open),
    select: (data) => data.data,
  });
};

export const useGetComponentDetailMapping = (
  productId: string,
  componentId: string,
  productType?: string,
  open = true
) => {
  return useQuery<any, Error, IDetailsData<IMapperDetails>>({
    queryKey: [
      PRODUCT_CACHE_KEYS.get_component_detail_mapping,
      productId,
      componentId,
    ],
    queryFn: () =>
      getComponentDetailMapping(productId, componentId, productType),
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
export const useEditComponentV2 = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.edit_component_detail],
    mutationFn: ({ productId, componentId, data }: any) =>
      editComponentDetail(productId, componentId, data),
    onSuccess: (data: any, variables: any) => {
      if (data?.code == 200) {
        queryClient.invalidateQueries({
          queryKey: [
            PRODUCT_CACHE_KEYS.get_component_detail,
            variables?.productId,
            variables?.componentId,
          ],
        });
      }
    },
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

export const useDeleteApiServer = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.delete_api_server],
    mutationFn: ({ productId, componentId }: any) =>
      deleteAPIServer(productId, componentId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.delete_api_server],
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

export const useRotateApiKey = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.rotate_api_key],
    mutationFn: (data: any): any => rotateApiKey(data),
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

export const useGetMapperDetails = (productId: string, componentId: string, productType?: string) => {
  return useQuery<any, Error, any>({
    queryKey: [PRODUCT_CACHE_KEYS.get_mapper_details, productId, componentId],
    queryFn: () => getMapperDetails(productId, componentId, productType),
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
  return useQuery<any, Error, IRunningMapping[]>({
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
  return useQuery<any, Error, IPagingData<IDeploymentHistory>>({
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

export const useGetAuditLogDetails = (
  params: Record<string, any>,
  id: string
) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_audit_log_details, params, id],
    queryFn: () => getAuditLogDetails(params, id),
    enabled: Boolean(id),
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
    staleTime: STALE_TIME,
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
  return useQuery<any, Error, IApiMapperDeployment[]>({
    queryKey: [PRODUCT_CACHE_KEYS.get_running_api_list, productId, mapperKey],
    queryFn: () => getLatestRunningAPI(productId, mapperKey),
    enabled: Boolean(productId) && Boolean(mapperKey),
    select: (data) => data?.data,
    staleTime: STALE_TIME,
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
  deploymentId: string
) => {
  return useQuery<any, Error, IDeploymentHistory[]>({
    queryKey: [PRODUCT_CACHE_KEYS.get_upgrade_detail, productId, deploymentId],
    queryFn: () => getMappingTemplateUpgradeDetail(productId, deploymentId),
    enabled: Boolean(productId) && Boolean(deploymentId),
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
        queryKey: [PRODUCT_CACHE_KEYS.get_release_list],
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
        queryKey: [PRODUCT_CACHE_KEYS.get_release_list],
      });
    },
  });
};

export const useActiveBuyer = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.active_buyer],
    mutationFn: ({ productId, id }: any) => activateBuyer(productId, id),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_buyer_list],
      });
    },
  });
};

export const useDeactiveBuyer = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.deactive_buyer],
    mutationFn: ({ productId, id }: any) => deactivateBuyer(productId, id),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_buyer_list],
      });
    },
  });
};

export const useRegenToken = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.regenerate_token],
    mutationFn: ({ productId, id }: any) =>
      regenerateBuyerAccessToken(productId, id),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_buyer_list],
      });
    },
  });
};

export const useGetValidateServerName = () => {
  return useMutation<any, Error, IProductIdAndNameParams>({
    mutationKey: [PRODUCT_CACHE_KEYS.get_validate_api_server_name],
    mutationFn: ({ productId, name }: IProductIdAndNameParams) =>
      getValidateServerName(productId, name),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_validate_api_server_name],
      });
    },
  });
};

export const useEditContactInformation = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.edit_contact_information],
    mutationFn: ({ productId, componentId, id, data }: any) =>
      editContactInformation(productId, componentId, id, data),
  });
};

export const useGetProductTypes = (productId: string) => {
  return useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_product_type_list, productId],
    queryFn: () => getProductTypes(productId),
    enabled: Boolean(productId),
    select: (data) => data?.data,
  });
};
