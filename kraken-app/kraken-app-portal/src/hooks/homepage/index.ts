import {
  getApiActivityRequests,
  getErrorBrakedownRequests,
  getMostPopularEndpointsRequests,
  getQuickStartGuide,
} from "@/services/homepage";
import {
  IApiActivity,
  IErrorBrakedown,
  IMostPopularEndpoints,
  IQuickStartGuideObject,
} from "@/utils/types/product.type";
import { useQuery } from "@tanstack/react-query";
import { USER_CACHE_KEYS } from "../user";

export const useGetQuickStartGuide = (productId: string, kind: string) => {
  return useQuery<any, Error, IQuickStartGuideObject>({
    queryKey: [USER_CACHE_KEYS.get_quick_start_guide],
    queryFn: () => getQuickStartGuide(productId, kind),
    select: (data) => data?.data,
  });
};

export const useGetActivityRequests = (
  productId: string,
  envId: string,
  requestStartTime?: string,
  requestEndTime?: string,
  buyer?: string
) => {
  return useQuery<any, Error, IApiActivity>({
    queryKey: [USER_CACHE_KEYS.get_quick_start_guide],
    queryFn: () =>
      getApiActivityRequests(productId, envId, {
        requestStartTime,
        requestEndTime,
        buyer,
      }),
    select: (data) => data?.data,
  });
};

export const useGetErrorBrakedown = (
  productId: string,
  envId: string,
  requestStartTime: undefined | string,
  requestEndTime: undefined | string
) => {
  return useQuery<any, Error, IErrorBrakedown>({
    queryKey: [USER_CACHE_KEYS.get_error_brakedown],
    queryFn: () =>
      getErrorBrakedownRequests(productId, envId, {
        requestStartTime,
        requestEndTime,
      }),
    select: (data) => data?.data,
  });
};

export const useGetMostPopularEndpoints = (
  productId: string,
  envId: string,
  requestStartTime: undefined | string,
  requestEndTime: undefined | string
) => {
  return useQuery<any, Error, IMostPopularEndpoints>({
    queryKey: [USER_CACHE_KEYS.get_most_popular_endpoints],
    queryFn: () =>
      getMostPopularEndpointsRequests(productId, envId, {
        requestStartTime,
        requestEndTime,
      }),
    select: (data) => data?.data,
  });
};
