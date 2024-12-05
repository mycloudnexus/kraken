import { createActivityHistoryLog, getPushButtonEnabledStatus, getPushEventHistory } from '@/services/pushApiActivity';
import { STALE_TIME } from '@/utils/constants/common';
import { ICreateActivityHistoryLogRequest, IActivityHistoryLog, IPagingData } from '@/utils/types/common.type';
import { useMutation, useQuery } from '@tanstack/react-query';
import { AxiosResponse } from 'axios';
import { queryClient } from "@/utils/helpers/reactQuery";

const PUSH_API_EVENT_CACHE_KEYS = {
  create_activity_history_log: "create_activity_history_log",
  get_product_env_list: "get_product_env_list",
  get_push_button_enabled: "get_push_button_enabled",
};

export const useGetPushActivityLogHistory = () => {
  return useQuery<AxiosResponse, Error, IPagingData<IActivityHistoryLog>>({
    queryKey: [PUSH_API_EVENT_CACHE_KEYS.get_product_env_list],
    queryFn: () => getPushEventHistory(),
    select: (data) => data.data,
    staleTime: STALE_TIME,
  });
}

export const useGetPushButtonEnabled = () => {
  return useQuery<AxiosResponse, Error, any>({
    queryKey: [PUSH_API_EVENT_CACHE_KEYS.get_push_button_enabled],
    queryFn: () => getPushButtonEnabledStatus(),
    select: (data) => data.data,
    staleTime: STALE_TIME,
  });
}

export const usePostPushActivityLog = () => {
  return useMutation<any, Error, ICreateActivityHistoryLogRequest>({
    mutationKey: [PUSH_API_EVENT_CACHE_KEYS.create_activity_history_log],
    mutationFn: (data) => {
      return createActivityHistoryLog(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [PUSH_API_EVENT_CACHE_KEYS.create_activity_history_log],
      });
    },
  });
};