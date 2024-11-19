import request from "@/utils/helpers/request";
import { PUSH_API_ACTIVITY } from "@/utils/constants/api";

export const getPushEventHistory = () =>
  request(`${PUSH_API_ACTIVITY}/history`);
