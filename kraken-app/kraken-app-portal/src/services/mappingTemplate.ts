import { PRODUCT, PRODUCT_V3 } from "@/utils/constants/api";
import request from "@/utils/helpers/request";

export const getMappingTemplateReleaseHistory = (
  productId: string,
  params: any
) => {
  return request(`${PRODUCT_V3}/${productId}/template-upgrade/releases`, {
    method: "GET",
    params,
  });
};

export const getTemplateMappingReleaseDetail = (
  productId: string,
  templateUpgradeId: string
) => {
  return request(`${PRODUCT_V3}/${productId}/template-upgrade/releases`, {
    method: "GET",
    params: {
      templateUpgradeId,
    },
  });
};

// Step 1
export function getListTemplateUpgradeApiUseCase(
  productId: string,
  templateUpgradeId: string
) {
  return request(`${PRODUCT_V3}/${productId}/template-upgrade/api-use-cases`, {
    params: {
      templateUpgradeId,
    },
  });
}

// Shares between step 1's right side panel and step2's left side panel
export function getListApiUseCase(productId: string) {
  return request(`${PRODUCT}/${productId}/api-use-cases`);
}

export function controlPlaneTemplateUpgrade(
  productId: string,
  data: { templateUpgradeId: string }
) {
  return request(`${PRODUCT_V3}/${productId}/template-upgrade/control-plane`, {
    method: "POST",
    data,
  });
}

// Shares between step 2's and step 3's panels
// envId = stageEnvId or productEnvId
export function getListDataPlaneUseCase(productId: string, envId: string) {
  return request(`${PRODUCT_V3}/${productId}/running-api-mapper-deployments`, {
    params: {
      envId,
    },
  });
}

// Step 2
export function stageTemplateUpgrade(
  productId: string,
  data: { templateUpgradeId: string; stageEnvId: string }
) {
  return request(`${PRODUCT_V3}/${productId}/template-upgrade/stage`, {
    method: "POST",
    data,
  });
}

// Step 3
export function productionTemplateUpgrade(
  productId: string,
  data: {
    templateUpgradeId: string;
    stageEnvId: string;
    productEnvId: string;
  }
) {
  return request(`${PRODUCT_V3}/${productId}/template-upgrade/production`, {
    method: "POST",
    data,
  });
}

export function checkStageUpgradeCheck(
  productId: string,
  templateUpgradeId: string,
  envId: string
) {
  return request(
    `${PRODUCT_V3}/${productId}/template-upgrade/stage-upgrade-check`,
    {
      params: {
        templateUpgradeId,
        envId,
      },
    }
  );
}

export function checkProductionUpgradeCheck(
  productId: string,
  templateUpgradeId: string,
  envId: string
) {
  return request(
    `${PRODUCT_V3}/${productId}/template-upgrade/production-upgrade-check`,
    {
      params: {
        templateUpgradeId,
        envId,
      },
    }
  );
}
