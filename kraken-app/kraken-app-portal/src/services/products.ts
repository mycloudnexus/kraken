import { PRODUCT } from "@/utils/constants/api";
import request from "@/utils/helpers/request";
import type { IPagingParams } from "@/utils/types/common.type";
import { ICreateParameter } from "@/utils/types/env.type";
import { INewVersionParams } from "@/utils/types/product.type";

export const getListComponents = (
  productId: string,
  params: Record<string, any>
) => {
  return request(`${PRODUCT}/${productId}/components`, {
    params,
  });
};

export const getListComponentsV2 = (
  productId: string,
  targetMapperKey: string
) => {
  return request(`${PRODUCT}/${productId}/components/${targetMapperKey}`);
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

export const getComponentDetailV2 = (
  productId: string,
  componentId: string
) => {
  return request(`/v2${PRODUCT}/${productId}/components/${componentId}`);
};

export const getComponentDetailMapping = (
  productId: string,
  componentId: string
) => {
  return request(
    `${PRODUCT}/${productId}/components/${componentId}/mapper-details`
  );
};

export const getComponentSpecDetails = (
  productId: string,
  componentId: string
) => {
  return request(
    `${PRODUCT}/${productId}/components/${componentId}/spec-details`
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

export const getAPIServers = (
  productId: string,
  params: Record<string, any>
) => {
  return request(
    `/v2${PRODUCT}/${productId}/components/${productId}/api-servers`,
    {
      params: { ...params, facetIncluded: true, liteSearch: true },
    }
  );
};

export const deleteAPIServer = (productId: string, componentId: string) => {
  return request(
    `/v2${PRODUCT}/${productId}/components/${componentId}/api-servers`,
    {
      method: "DELETE",
    }
  );
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

export const getBuyerList = (
  productId: string,
  params: Record<string, any>
) => {
  return request(`${PRODUCT}/${productId}/buyers`, {
    method: "GET",
    params,
  });
};

export const getAuditLogs = (params: Record<string, any>) => {
  return request(`/audit/logs`, {
    method: "GET",
    params,
  });
};

export const getAuditLogDetails = (params: Record<string, any>, id: string) => {
  return request(`/audit/logs/${id}`, {
    method: "GET",
    params,
  });
};

export const createBuyer = (productId: string, data: any) => {
  return request(`${PRODUCT}/${productId}/buyers`, {
    method: "POST",
    data,
  });
};

export const verifyProduct = (productId: string, data: any) => {
  return request(`/v2${PRODUCT}/${productId}/verify-api-mapper-in-labels`, {
    method: "PATCH",
    data,
  });
};

export const deployProduction = (productId: string, data: any) => {
  return request(`/v2${PRODUCT}/${productId}/deploy-stage-to-production`, {
    method: "POST",
    data,
  });
};

export const getLatestRunningAPI = (productId: string, mapperKey: string) => {
  return request(
    `/v2${PRODUCT}/${productId}/latest-running-api-mapper-deployments?mapperKey=${mapperKey}`
  );
};

export const getMappingTemplateReleaseHistory = (
  productId: string,
  params: any
) => {
  return request(`/v2${PRODUCT}/${productId}/template-upgrade/releases`, {
    method: "GET",
    params,
  });
};

export const getMappingTemplateCurrentVersion = (productId: string) => {
  return request(
    `/v2${PRODUCT}/${productId}/template-upgrade/current-versions`,
    {
      method: "GET",
    }
  );
};

export const getMappingTemplateUpgradeList = (
  productId: string,
  params: any
) => {
  return request(
    `/v2${PRODUCT}/${productId}/template-upgrade/template-deployments`,
    {
      method: "GET",
      params,
    }
  );
};

export const getMappingTemplateUpgradeDetail = (
  productId: string,
  id: string
) => {
  return request(
    `/v2${PRODUCT}/${productId}/template-upgrade/template-deployments/${id}`,
    {
      method: "GET",
    }
  );
};

export const productUpgradeStage = (productId: string, data: any) => {
  return request(`/v2${PRODUCT}/${productId}/template-upgrade/stage`, {
    method: "POST",
    data,
  });
};

export const productUpgradeProd = (productId: string, data: any) => {
  return request(`/v2${PRODUCT}/${productId}/template-upgrade/production`, {
    method: "POST",
    data,
  });
};

export const activateBuyer = (productId: string, id: string) => {
  return request(`${PRODUCT}/${productId}/buyers/${id}/activate`, {
    method: "POST",
  });
};

export const deactivateBuyer = (productId: string, id: string) => {
  return request(`${PRODUCT}/${productId}/buyers/${id}/deactivate`, {
    method: "POST",
  });
};

export const regenerateBuyerAccessToken = (productId: string, id: string) => {
  return request(`${PRODUCT}/${productId}/buyers/${id}/access-tokens`, {
    method: "POST",
  });
};

export const getValidateServerName = (productId: string, name: string) => {
  return request(
    `/v2${PRODUCT}/${productId}/components/${productId}/api-servers/${name}`,
    {
      method: "GET",
    }
  );
};

export const editContactInformation = (
  productId: string,
  componentId: string,
  id: string,
  data: any
) => {
  return request(
    `${PRODUCT}/${productId}/components/${componentId}/seller-contacts/${id}`,
    {
      method: "PATCH",
      data,
    }
  );
};
