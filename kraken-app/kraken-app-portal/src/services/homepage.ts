import request from "@/utils/helpers/request";

export const getQuickStartGuide = (productId: string, kind: string) => {
  return request(`/start/guide/${productId}`, {
    method: "GET",
    params: { kind },
  });
};

export const getApiActivityRequests = (
  productId: string,
  envId: string,
  params: object
) => {
  return request(
    `/products/${productId}/envs/${envId}/statistics/api-activity-requests`,
    {
      method: "GET",
      params,
    }
  );
};

export const getErrorBrakedownRequests = (
  productId: string,
  envId: string,
  params: object
) => {
  return request(
    `/products/${productId}/envs/${envId}/statistics/error-requests`,
    {
      method: "GET",
      params,
    }
  );
};

export const getMostPopularEndpointsRequests = (
  productId: string,
  envId: string,
  params: object
) => {
  return request(
    `/products/${productId}/envs/${envId}/statistics/most-popular-endpoint`,
    {
      method: "GET",
      params,
    }
  );
};

export const getProductTypeList = (productId: string) => {
  return request(`/products/${productId}/productTypes`, {
    method: "GET",
  });
};

export const getStandardApiComponents = (
  productId: string,
  productType: string
) => {
  return request(`/products/${productId}/standardApiComponents`, {
    method: "GET",
    params: { productType },
  });
};
