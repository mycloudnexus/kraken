import { getComponentAPIDoc, getListComponents } from "@/services/products";
import { useQuery } from "@tanstack/react-query";
import { get } from "lodash";

export const PRODUCT_CACHE_KEYS = {
  get_product_component_list: "get_product_component_list",
  get_component_api_doc: "get_component_api_doc",
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
