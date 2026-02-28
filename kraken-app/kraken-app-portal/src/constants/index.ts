export const ENV = {
  API_BASE_URL: import.meta.env.VITE_BASE_API,
  AUTHENTICATION_TYPE: import.meta.env.VITE_AUTHENTICATION_TYPE ?? "basic",
  PRODUCT_ID: import.meta.env.VITE_PRODUCT_ID ?? "mef.sonata",
  PRODUCT_NAME: import.meta.env.VITE_PRODUCT_NAME ?? "MEF LSO API Adaptor",
  VIEW_BUYER_TOKEN: import.meta.env.VITE_VIEW_TOKEN ?? 'false'
}
