import { QueryClient } from "@tanstack/react-query";

export const QUERY_CLIENT_DEFAULT_OPTIONS = {
  defaultOptions: {
    queries: {
      retry: false,
      staleTime: 5 * 1000,
      refetchOnWindowFocus: false,
    },
  },
};

export const queryClient = new QueryClient(QUERY_CLIENT_DEFAULT_OPTIONS);
