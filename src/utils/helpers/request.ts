import { message, notification } from "antd";
import axios, { AxiosResponse } from "axios";
import qs from "qs";
import { clearData, getData, isRefreshTokenExpired, storeData } from "./token";
import { AXIOS_MESSAGE } from "../constants/message";
import { ROUTES } from "../constants/route";
import { get } from "lodash";
import createAuthRefreshInterceptor from "axios-auth-refresh";

export const DIRECT_LOGIN_MSG = [
  AXIOS_MESSAGE.TOKEN_EXPIRED,
  AXIOS_MESSAGE.TOKEN_INVALID,
];

export const refreshTokenFnc = async () => {
  const token = getData("token");
  const refreshToken = getData("refreshToken");
  if (!refreshToken || isRefreshTokenExpired() || !token) {
    clearData("token");
    clearData("tokenExpired");
    window.location.href = `${window.location.origin}${ROUTES.LOGIN}`;
    return;
  }
  try {
    const res = await axios.post(
      import.meta.env.VITE_PCCW_BASE_API + "/auth/token",
      {
        refreshToken,
        grantType: "REFRESH_TOKEN",
      },
      {
        headers: {
          Authorization: `Bearer ${getData("token")}`,
        },
      }
    );

    const expiresIn = get(res, "data.data.expiresIn");
    const nToken = get(res, "data.data.accessToken");
    const refreshTokenExpiresIn = get(res, "data.data.refreshTokenExpiresIn");
    const newRefreshToken = get(res, "data.data.refreshToken");
    if (nToken && expiresIn) {
      storeData("token", nToken);
      storeData("refreshToken", newRefreshToken);
      storeData("tokenExpired", String(Date.now() + expiresIn * 1000));
      storeData(
        "refreshTokenExpiresIn",
        String(Date.now() + refreshTokenExpiresIn * 1000)
      );
    }
  } catch (e) {
    void message.error("Your session has expired. Please log in again.");
    clearData("token");
    clearData("tokenExpired");
    window.location.href = `${window.location.origin}${ROUTES.LOGIN}`;
    return Promise.reject(new Error("Token expired"));
  }
};

const request = axios.create({
  timeout: 50000,
  baseURL: import.meta.env.VITE_PCCW_BASE_API,
});

createAuthRefreshInterceptor(request, refreshTokenFnc, {
  statusCodes: [401],
  pauseInstanceWhileRefreshing: true,
  onRetry: (requestConfig) => {
    return {
      ...requestConfig,
      headers: {
        ...requestConfig.headers,
        Authorization: `Bearer ${getData("token")}`,
      },
      skipAuthRefresh: false,
    };
  },
  shouldRefresh: (error) => {
    const errorMsg = get(error, "response.data.error", "");
    return (
      DIRECT_LOGIN_MSG.includes(String(errorMsg)) ||
      error?.response?.status === 401
    );
  },
});

request.interceptors.request.use((config: any) => {
  const token = getData("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  if (config.method === "get") {
    config.paramsSerializer = {
      serialize: (params: any) =>
        qs.stringify(params, { arrayFormat: "repeat" }),
    };
  }
  return config;
});

request.interceptors.response.use(
  (response: AxiosResponse<any>) => {
    const code = response.data.code || 200;
    const message = response.data.error || "";
    if (
      (code === 401 || code === 403) &&
      process.env.NODE_ENV !== "development"
    ) {
      notification.error({ message });
    }
    return response.data;
  },
  (err) => {
    return Promise.reject(get(err, "response.data", err));
  }
);

export default request;
