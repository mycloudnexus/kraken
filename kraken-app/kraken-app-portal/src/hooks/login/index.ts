import { login } from "@/components/AuthProviders/basic/helper/auth";
import { useMutation } from "@tanstack/react-query";

const LOGIN_CACHE_KEYS = {
  login: "login",
};

export const useLogin = () =>
  useMutation<any, Error>({
    mutationKey: [LOGIN_CACHE_KEYS.login],
    mutationFn: (data: unknown) => login(data),
  });
