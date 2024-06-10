import { PRODUCT } from "@/utils/constants/api";
import request from "@/utils/helpers/request";

export const getListComponents = (
  productId: string,
  params: Record<string, any>
) => {
  return request(`${PRODUCT}/${productId}/components`, {
    params,
  });
};

export const getComponentAPIDoc = (productId: string, componentId: string) => {
  return request(`${PRODUCT}/${productId}/components/${componentId}/api-docs`);
};

export const createNewComponent = (productId: string, data: any) => {
  return request(`${PRODUCT}/${productId}/components`, {
    method: "POST",
    data,
  });
};
