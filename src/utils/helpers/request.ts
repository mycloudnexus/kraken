import { message, notification } from "antd";
import axios, {
  AxiosError,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from "axios";
import qs from "qs";
import { clearData, getData, storeData } from "./token";
import { AXIOS_MESSAGE } from "../constants/message";
import { ROUTES } from "../constants/route";
import { get } from "lodash";

const DIRECT_LOGIN_MSG = [
  AXIOS_MESSAGE.TOKEN_EXPIRED,
  AXIOS_MESSAGE.TOKEN_INVALID,
];
const onError = (error: AxiosError) => {
  if (error.response) {
    const errorMsg = (error.response.data as any)?.error;
    if (DIRECT_LOGIN_MSG.includes(String(errorMsg)) || error.response.status === 401) {
      storeData("lastVisitedPath", window.location.pathname)
      void message.error("Your session has expired. Please log in again.");
      clearData("token");
      window.location.href = ROUTES.LOGIN;
    }

    // Request was made but server responded with something
    // other than 2xx
    console.error("Status:", error.response.status);
    console.error("Data:", error.response.data);
    console.error("Headers:", error.response.headers);
  } else {
    // Something else happened while setting up the request
    // triggered the error
    console.error("Error Message:", error.message);
  }

  return Promise.reject(
    get(
      error,
      "response.data",
      get(error, "message", "Error. Please contact administrator.")
    )
  );
};

const request = axios.create({
  timeout: 50000,
  baseURL: import.meta.env.VITE_PCCW_BASE_API,
});
request.interceptors.request.use(
  (config): InternalAxiosRequestConfig<any> => {
    if (config.method === "get") {
      config.paramsSerializer = {
        serialize: (params: Record<string, any>) =>
          qs.stringify(params, { arrayFormat: "repeat" }),
      };
    }
    return config;
  },
  (err) => {
    return onError(err);
  }
);

request.interceptors.request.use(
  (config: any) => {
    const token = getData("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    if (config.method === "get") {
      config.paramsSerializer = {
        serialize: (params: any) => {
          return qs.stringify(params, { arrayFormat: "comma" });
        },
      };
    }
    return config;
  },
  (err) => {
    return onError(err);
  }
);

request.interceptors.response.use(
  (response: AxiosResponse<any>) => {
    const code = response.data.code || 200;
    const message = response.data.error || "";
    if ((code === 401 || code === 403) && process.env.VITE_APP_ENV !== "dev") {
      notification.error({ message });
    }
    return response.data;
  },
  (err) => {
    return onError(err);
  }
);

export default request;
