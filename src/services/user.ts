import { USER } from "@/utils/constants/api";
import request from "@/utils/helpers/request";

export const getListUser = (params: any) => {
  return request(`${USER}`, {
    method: "GET",
    params,
  });
};
