import { PRODUCT } from "@/utils/constants/api";
import request from "@/utils/helpers/request";
import { INewVersionParams } from "@/utils/types/product.type";
import { ICreateParameter } from "@/utils/types/env.type";

import type { IPagingParams } from "@/utils/types/common.type";

export const getListComponentsV2 = (
  productId: string,
  componentId: string
) => {
  return request(`${PRODUCT}/${productId}/components/${componentId}`);
};

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

export const getComponentDetailMapping = (
  productId: string,
  componentId: string
) => {
  return request(
    `${PRODUCT}/${productId}/components/${componentId}/mapper-details`
  );
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

export const getAllApiKeyList = (productId: string, params: IPagingParams) => {
  return request(`${PRODUCT}/${productId}/env-api-tokens`, {
    method: "GET",
    params,
  });
};

export const createApiKey = (payload: ICreateParameter) => {
  const { productId, envId, name } = payload;
  return request(`${PRODUCT}/${productId}/envs/${envId}/api-tokens`, {
    method: "POST",
    data: {
      name,
    },
  });
};

export const getAllDataPlaneList = (
  productId: string,
  params: IPagingParams
) => {
  return request(`${PRODUCT}/${productId}/env-clients`, {
    method: "GET",
    params,
  });
};
export const getRunningComponentList = (productId: string) => {
  return request(`${PRODUCT}/${productId}/running-components`, {
    method: "GET",
  });
};

export const createNewVersion = (data: INewVersionParams) => {
  const { productId, componentId, componentKey, versionName, version } = data;
  return request(`${PRODUCT}/${productId}/components/${componentId}/versions`, {
    method: "POST",
    data: {
      componentKey,
      versionName,
      version,
    },
  });
};

export const getVersionList = (
  productId: string,
  componentId: string,
  params: Record<string, any>
) => {
  return request(`${PRODUCT}/${productId}/components/${componentId}/versions`, {
    method: "GET",
    params,
  });
};

export const getRunningVersionList = (
  productId: string,
  componentId: string
) => {
  return request(
    `${PRODUCT}/${productId}/components/${componentId}/running-versions`,
    {
      method: "GET",
    }
  );
};

export const updateTargetMapper = (
  productId: string,
  componentId: string,
  data: unknown
) => {
  return request(
    `${PRODUCT}/${productId}/components/${componentId}/targetMapper`,
    {
      method: "PATCH",
      data,
    }
  );
};

export const getMapperDetails = (productId: string, componentId: string) => {
  return request(
    `${PRODUCT}/${productId}/components/${componentId}/mapper-details`,
    {
      method: "GET",
    }
  );
};

export const deployToEnv = (
  productId: string,
  componentId: string,
  mapperKeys: string[],
  envId: string
) => {
  return request(`/v2${PRODUCT}/${productId}/api-mapper-deployments`, {
    method: "POST",
    data: {
      componentId,
      mapperKeys,
      envId,
    },
  });
};

export const getRunningAPIMappingList = (
  productId: string,
  params: Record<string, any>
) => {
  return request(`/v2${PRODUCT}/${productId}/running-api-mapper-deployments`, {
    method: "GET",
    params,
  });
};

export const getAPIMapperDeployments = (
  productId: string,
  params: Record<string, any>
) => {
  return request(`/v2${PRODUCT}/${productId}/api-mapper-deployments`, {
    method: "GET",
    params,
  });
};
