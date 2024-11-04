import { UseQueryResult } from "@tanstack/react-query";
import { useEffect } from "react";

export function useLongPolling<T>(
  queryResult: UseQueryResult<T, Error>,
  ms: number
) {
  const { data, isLoading, isFetching, refetch } = queryResult;

  useEffect(() => {
    const intervalId = setInterval(refetch, ms);

    return () => {
      clearInterval(intervalId);
    };
  }, []);

  return {
    data,
    isLoading,
    isFetching,
    refetch,
  };
}
