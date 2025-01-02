import request from "@/utils/helpers/request";
import { PUSH_API_ACTIVITY } from "@/utils/constants/api";
import { ICreateActivityHistoryLogRequest } from '@/utils/types/common.type';

export const getPushEventHistory = () =>
  request(`${PUSH_API_ACTIVITY}/history`);

export const getPushButtonEnabledStatus = () =>
  request(`${PUSH_API_ACTIVITY}/enabled`);

export const createActivityHistoryLog = (payload: ICreateActivityHistoryLogRequest) => {
  return request(PUSH_API_ACTIVITY, {
    method: "POST",
    data: {
      ...payload,
    },
  });
};
