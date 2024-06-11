import {
  createNewComponent,
  getComponentAPIDoc,
  getEnvActivity,
  getListComponents,
  getListEnvActivities,
  getListEnvs,
} from "@/services/products";
import { IPagingData } from "@/utils/types/common.type";
import { IActivityDetail, IActivityLog, IEnv } from "@/utils/types/env.type";
import { useMutation, useQuery } from "@tanstack/react-query";
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
  const { data, ...result } = useQuery<any, Error>({
    queryKey: [PRODUCT_CACHE_KEYS.get_component_list, productId, params],
    queryFn: () => getListComponents(productId, params),
    enabled: Boolean(productId),
  });
  return {
    data: get(data, "data"),
    ...result,
  };
};

export const useManualGetComponentList = () => {
  return useMutation<any, Error>({
    mutationKey: [PRODUCT_CACHE_KEYS.get_component_list],
    mutationFn: ({ productId, params }: any) =>
      getListComponents(productId, params),
  });
};

export const useGetProductEnvs = (productId: string) => {
  return useQuery<AxiosResponse, Error, IPagingData<IEnv>>({
    queryKey: [PRODUCT_CACHE_KEYS.get_product_env_list, productId],
    queryFn: () => getListEnvs(productId),
    enabled: Boolean(productId),
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
