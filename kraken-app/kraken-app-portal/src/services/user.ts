import { SIGN_UP, USER, USER_AUTH_TOKEN, USER_ROLES } from "@/utils/constants/api";
import request from "@/utils/helpers/request";
import { SignUpPayload, ResponseType } from "./types";

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

export const resetPwdUser = (id: string, password: string) => {
  return request(`${USER}/${id}/resetPassword`, {
    method: "POST",
    data: {
      password,
    },
  });
};

export function getSystemInfo() {
  return request(`system-info`);
};

export const getUserDetail = (name: string) => {
  return request(`/userinfo`);
}

export const getUserAuthToken = () => {
  return request(USER_AUTH_TOKEN)
}

export const getUserRoles = () => {
  return request(USER_ROLES)
}

export const signUpUser = (data: SignUpPayload): Promise<ResponseType<any>> => {
  return request(SIGN_UP, {
    method: 'post',
    data
  })
}
