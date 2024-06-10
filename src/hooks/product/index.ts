import {
  createNewComponent,
  getComponentAPIDoc,
  getListComponents,
} from "@/services/products";
import { useMutation, useQuery } from "@tanstack/react-query";
import { get } from "lodash";

export const PRODUCT_CACHE_KEYS = {
  get_product_component_list: "get_product_component_list",
  get_component_api_doc: "get_component_api_doc",
  create_new_component: "create_new_component",
  get_component_list: "get_component_list",
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
