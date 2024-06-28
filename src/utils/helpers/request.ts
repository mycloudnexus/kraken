import axios, {
  AxiosError,
  InternalAxiosRequestConfig,
  AxiosResponse,
} from "axios";
import qs from "qs";
import { notification } from "antd";

const onError = (error: AxiosError) => {
  console.error("Request Failed:", error.config);

  if (error.response) {
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

  return Promise.reject(error.response || error.message);
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
