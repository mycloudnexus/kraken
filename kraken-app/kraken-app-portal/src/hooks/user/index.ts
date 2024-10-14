import { getListUser } from "@/services/user";
import { useQuery } from "@tanstack/react-query";

export const USER_CACHE_KEYS = {
  get_user: "get_user",
};
export const useGetUserList = (params: Record<string, any>, options?: any) => {
  return useQuery<any, Error>({
    queryKey: [USER_CACHE_KEYS.get_user, params],
    queryFn: () => getListUser(params),
    ...options,
    select: (data) => data?.data,
  });
};
