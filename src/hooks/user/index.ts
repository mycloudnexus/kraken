import {
  createUser,
  disableUser,
  editUser,
  enableUser,
  getCurrentUser,
  getListUser,
  resetPwdUser,
} from "@/services/user";
import { queryClient } from "@/utils/helpers/reactQuery";
import { useMutation, useQuery } from "@tanstack/react-query";

export const USER_CACHE_KEYS = {
  get_user: "get_user",
  get_current_user: "get_current_user",
  create_user: "create_user",
  edit_user: "edit_user",
  enable_user: "enable_user",
  disable_user: "disable_user",
  reset_password: "reset_password",
};
export const useGetUserList = (params: Record<string, any>, options?: any) => {
  return useQuery<any, Error>({
    queryKey: [USER_CACHE_KEYS.get_user, params],
    queryFn: () => getListUser(params),
    select: (data) => data?.data,
    ...options,
  });
};

export const useGetCurrentUser = () => {
  return useQuery<any, Error>({
    queryKey: [USER_CACHE_KEYS.get_current_user],
    queryFn: () => getCurrentUser(),
    select: (data) => data?.data,
    staleTime: 999999,
  });
};

export const useCreateUser = () => {
  return useMutation<any, Error>({
    mutationKey: [USER_CACHE_KEYS.create_user],
    mutationFn: (data: any) => createUser(data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [USER_CACHE_KEYS.get_user],
      });
    },
  });
};

export const useEditUser = () => {
  return useMutation<any, Error>({
    mutationKey: [USER_CACHE_KEYS.edit_user],
    mutationFn: (data: any) => editUser(data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [USER_CACHE_KEYS.get_user],
      });
    },
  });
};
export const useEnableUser = () => {
  return useMutation<any, Error>({
    mutationKey: [USER_CACHE_KEYS.enable_user],
    mutationFn: (id: any) => enableUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [USER_CACHE_KEYS.get_user],
      });
    },
  });
};
export const useDisableUser = () => {
  return useMutation<any, Error>({
    mutationKey: [USER_CACHE_KEYS.disable_user],
    mutationFn: (id: any) => disableUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: [USER_CACHE_KEYS.get_user],
      });
    },
  });
};

export const useResetPassword = () => {
  return useMutation<any, Error>({
    mutationKey: [USER_CACHE_KEYS.reset_password],
    mutationFn: (data: any) => resetPwdUser(data.id, data.password),
  });
};
