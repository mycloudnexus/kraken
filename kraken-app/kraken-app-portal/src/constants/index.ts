export const ENV = {
  API_BASE_URL: import.meta.env.VITE_BASE_API,
  AUTHENTICATION_TYPE: import.meta.env.VITE_AUTHENTICATION_TYPE ?? "basic",
  VIEW_BUYER_TOKEN: import.meta.env.VITE_VIEW_TOKEN ?? 'false'
}
