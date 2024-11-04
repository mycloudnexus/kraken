import { UseQueryResult } from "@tanstack/react-query";
import { useEffect } from "react";

export function useLongPolling<T>(
  queryResult: UseQueryResult<T, Error>,
  ms: number,
  { active = true } = {}
) {
  const { data, isLoading, isFetching, refetch } = queryResult;

  useEffect(() => {
    if (active) {
      const intervalId = setInterval(refetch, ms);

      return () => {
        clearInterval(intervalId);
      };
    }
  }, [active]);

  return {
    data,
    isLoading,
    isFetching,
    refetch,
  };
}
