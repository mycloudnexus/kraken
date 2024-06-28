export const ROUTES = {
  ENV_OVERVIEW: "/env",
  ENV_ACTIVITY_LOG: (envId: string) => `/env/${envId}`,
  API_MAPPING: (componentId: string) => `/api-mapping/${componentId}`,
  NEW_API_MAPPING: (componentId: string) => `/api-mapping/${componentId}/new`,
};
