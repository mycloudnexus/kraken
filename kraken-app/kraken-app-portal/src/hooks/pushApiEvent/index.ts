import { getPushEventHistory } from '@/services/pushApiActivity';
import { STALE_TIME } from '@/utils/constants/common';
import { IPagingData } from '@/utils/types/common.type';
import { useQuery } from '@tanstack/react-query';
import { AxiosResponse } from 'axios';
import { PRODUCT_CACHE_KEYS } from '../product';

export const useGetPushActivityLogHistory = () => {
  return useQuery<AxiosResponse, Error, IPagingData<any>>({ // TODO: type push history
    queryKey: [PRODUCT_CACHE_KEYS.get_product_env_list],
    queryFn: () => getPushEventHistory(),
    select: (data) => data.data,
    staleTime: STALE_TIME,
  });
}