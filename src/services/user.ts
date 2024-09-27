import { USER } from "@/utils/constants/api";
import request from "@/utils/helpers/request";

export const getListUser = (params: any) => {
  return request(`${USER}`, {
    method: "GET",
    params,
  });
};

export const getCurrentUser = () => {
  return request(`/userinfo`);
};

export const createUser = (data: any) => {
  return request(`${USER}`, {
    method: "POST",
    data,
  });
};

export const editUser = (data: any) => {
  return request(`${USER}/${data.id}`, {
    method: "PATCH",
    data,
  });
};

export const enableUser = (id: string) => {
  return request(`${USER}/${id}/enable`, {
    method: "PATCH",
  });
};

export const disableUser = (id: string) => {
  return request(`${USER}/${id}/disable`, {
    method: "PATCH",
  });
};
