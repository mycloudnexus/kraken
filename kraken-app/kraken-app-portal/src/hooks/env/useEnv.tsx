import { useCallback } from "react";
import { useGetProductEnvs } from "../product";

export function useEnv(productId: string) {
  const {
    data: environments,
    isLoading,
    isFetching,
  } = useGetProductEnvs(productId);

  const findEnvByName = useCallback(
    (envName: string) => {
      return environments?.data?.find((env) => env.name === envName);
    },
    [environments]
  );

  return {
    environments,
    isLoading: isLoading || isFetching,
    findEnvByName,
  };
}
