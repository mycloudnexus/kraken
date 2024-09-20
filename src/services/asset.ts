import { ASSET } from "@/utils/constants/api";
import request from "@/utils/helpers/request";

export const getAsset = (id: string) => {
  return request(`${ASSET}/${id}`, {
    method: "GET",
  });
};
