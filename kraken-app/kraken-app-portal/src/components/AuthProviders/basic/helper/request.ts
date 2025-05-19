
import axios, { AxiosError, AxiosResponse, isCancel } from 'axios'
import _ from 'lodash'
import { ENV } from '@/constants'
import { ROUTES } from '@/utils/constants/route'
import { clearData, getData, isRefreshTokenExpired, storeData } from '@/utils/helpers/token';
import { message } from 'antd';
import createAuthRefreshInterceptor from 'axios-auth-refresh';
import { AXIOS_MESSAGE } from '@/utils/constants/message';
import { refresh } from './refresh';

export const DIRECT_LOGIN_MSG = [
  AXIOS_MESSAGE.TOKEN_EXPIRED,
  AXIOS_MESSAGE.TOKEN_INVALID,
];

export const refreshTokenFnc = async () => {
  const token = getData("token");
  const refreshToken = getData("refreshToken");
  if (!refreshToken || isRefreshTokenExpired() || !token) {
    handleExpiration();
    return;
  }
  try {
    const res = await refresh(refreshToken);

    const expiresIn = _.get(res, "data.data.expiresIn");
    const nToken = _.get(res, "data.data.accessToken");
    const refreshTokenExpiresIn = _.get(res, "data.data.refreshTokenExpiresIn") ?? 0;
    const newRefreshToken = _.get(res, "data.data.refreshToken") ?? "";
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
    console.error("Faied to refresh token:", e);
    handleExpiration();
    return Promise.reject(new Error("Reresh token failure"));
  }
};

interface TokenConfig {
  keyToken?: string
  tokenExpired?: string
  refreshToken?: string
  refreshTokenExpiresIn?: string
}

export const refreshTokenWithConfig = async (config : TokenConfig) => {
  const token = getData(config.keyToken ?? "token");
  const refreshToken = getData(config.refreshToken ?? "refreshToken");
  if (!refreshToken || isRefreshTokenExpired() || !token) {
    handleExpiration();
    return;
  }
  const res = await refresh(refreshToken);

  const expiresIn = _.get(res, "data.data.expiresIn");
  const nToken = _.get(res, "data.data.accessToken");
  const refreshTokenExpiresIn = _.get(res, "data.data.refreshTokenExpiresIn") ?? 0;
  const newRefreshToken = _.get(res, "data.data.refreshToken") ?? "";
  if (nToken && expiresIn) {
    storeData(config.keyToken ?? "token", nToken);
    storeData(config.refreshToken ?? "refreshToken", newRefreshToken);
    storeData(config.tokenExpired ?? "tokenExpired", String(Date.now() + expiresIn * 1000));
    storeData(
      config.tokenExpired ?? "refreshTokenExpiresIn",
      String(Date.now() + refreshTokenExpiresIn * 1000)
    );
  }
};

const handleExpiration = () => {
  void message.error("Your session has expired. Please log in again.");
  clearData("token");
  clearData("tokenExpired");
  window.location.href = `${window.location.origin}${ROUTES.LOGIN}`;
}

const BasicRequest = axios.create({
  timeout: 50000,
  baseURL: ENV.API_BASE_URL
})
BasicRequest.interceptors.request.use(async (config) => {
  return updateToken(config);
})

export const updateToken = async (config: any) => {
  const { getAccessToken } = window.portalConfig ?? {}
  if (!_.isFunction(window?.portalConfig?.getAccessToken)) {
    return config
  }
  const token = await getAccessToken?.({
    authorizationParams: {
      redirect_uri: window.location.origin
    }
  })
  config.headers.Authorization = `Bearer ${token}`
  return config;
}

createAuthRefreshInterceptor(BasicRequest, refreshTokenFnc, {
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
    const errorMsg = _.get(error, "response.data.error", "");
    return (
      DIRECT_LOGIN_MSG.includes(String(errorMsg)) ||
      error?.response?.status === 401
    );
  },
});

BasicRequest.interceptors.request.use((config: any) => {
  const currentCompany = window.sessionStorage.getItem('currentCompany')
  if (currentCompany) {
    config.headers['x-mef-lso-api-adapter-customer-id'] = currentCompany
  }
  return config
})

BasicRequest.interceptors.response.use(
  (response: AxiosResponse) => response.data,
  (error: AxiosError) => {
    const status = _.get(error, 'response.status')
    const message = _.get(error, 'response.data.error.message')
    const principalId = _.get(error, 'response.data.error.details.principalId')
    const pbacErrorEmptyPrincipal =
      status === 401 && message === accessDenied && _.isPlainObject(principalId) && _.isEmpty(principalId)
    const sessionExpired = status === 401 && invalidToken.includes(message!)
    if (pbacErrorEmptyPrincipal || sessionExpired) {
      const origin = window.location.origin
      window.location.href = origin +"/login/sso"
    }
    return Promise.reject(error)
  }
)

export const handleResponseError = (error: AxiosError) => {
  const status = _.get(error, 'response.status')
  const message = _.get(error, 'response.data.error.message')
  const principalId = _.get(error, 'response.data.error.details.principalId')
  const pbacErrorEmptyPrincipal =
    status === 401 && message === accessDenied && _.isPlainObject(principalId) && _.isEmpty(principalId)
  const sessionExpired = status === 401 && invalidToken.includes(message!)
  if (pbacErrorEmptyPrincipal || sessionExpired) {
    window.location.href = `${window.location.origin}${ROUTES.LOGIN}`
  }
  const statusCode = parseInt(status as unknown as string);
  if (statusCode < 300) {
    _.unset(error, 'response.data.error.message')
  } else if (statusCode === 400) {
    _.set(error, 'response.data.error.message', "Bad Request: " + message)
  } else if (statusCode === 401) {
    _.set(error, 'response.data.error.message', "Unauthorized: " + message);
  } else if (statusCode === 403) {
    _.set(error, 'response.data.error.message', "Forbidden: " + message);
  } else if (statusCode === 404) {
    _.set(error, 'response.data.error.message', "Not Found: " + message);
  } else if (statusCode === 405) {
    _.set(error, 'response.data.error.message', "method not allowed" + message);
  } else if (statusCode >= 500) {
    _.set(error, 'response.data.error.message', "Internal Error: " + message);
  } else {
    _.set(error, 'response.data.error.message', "failed: " + message);
  }
  return error;
}

export const isCancelCaught = (thrown: object) => isCancel(thrown)

const invalidToken = ['The user is not logged in', 'The session token has expired', 'The session token been deleted']

const accessDenied = 'Access denied'

export const fetchData = (path: string, config?: any) => BasicRequest.get(path, config).then((value) => value.data)

export const fetchCollection = (path: string) => fetchData(path).then((d: { results: any[] }) => d.results)

export const post = <ResponseBody = any>(path: string, data: any, config?: any) =>
  BasicRequest.post<ResponseBody>(path, data, config)

export const get = <ResponseBody = any>(path: string, config?: any) => BasicRequest.get<ResponseBody>(path, config)

export const patch = <ResponseBody = any>(path: string, data: any, config?: any) =>
  BasicRequest.patch<ResponseBody>(path, data, config)

export const deleteData = <ResponseBody = any>(path: string, config?: any) => BasicRequest.delete<ResponseBody>(path, config)

export default BasicRequest
