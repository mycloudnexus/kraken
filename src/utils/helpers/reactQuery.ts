import { QueryClient } from "@tanstack/react-query";

export const QUERY_CLIENT_DEFAULT_OPTIONS = {
  defaultOptions: {
    queries: {
      retry: 2,
      staleTime: 5 * 1000,
      refetchOnWindowFocus: false,
      retryDelay: 1100,
    },
  },
};

export const queryClient = new QueryClient(QUERY_CLIENT_DEFAULT_OPTIONS);
