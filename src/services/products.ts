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

export const getListEnvs = (productId: string) =>
  request(`${PRODUCT}/${productId}/envs`);

export const getListEnvActivities = (
  productId: string,
  envId: string,
  params: unknown
) =>
  request(`${PRODUCT}/${productId}/envs/${envId}/api-activities`, {
    params,
  });

export const getEnvActivity = (
  productId: string,
  envId: string,
  activityId: string
) =>
  request(`${PRODUCT}/${productId}/envs/${envId}/api-activities/${activityId}`);
export const getComponentDetail = (productId: string, componentId: string) => {
  return request(`${PRODUCT}/${productId}/components/${componentId}`);
};

export const editComponentDetail = (
  productId: string,
  componentId: string,
  data: any
) => {
  return request(`${PRODUCT}/${productId}/components/${componentId}`, {
    method: "PATCH",
    data,
  });
};

export const getListDeployments = (productId: string, params: unknown) =>
  request(`${PRODUCT}/${productId}/deployments`, {
    params,
  });

export const getListComponentVersions = (productId: string) =>
  request(`${PRODUCT}/${productId}/component-versions`);

export const deployProduct = (
  productId: string,
  envId: string,
  data: unknown
) =>
  request(`${PRODUCT}/${productId}/envs/${envId}/deployment`, {
    method: "POST",
    data,
  });
