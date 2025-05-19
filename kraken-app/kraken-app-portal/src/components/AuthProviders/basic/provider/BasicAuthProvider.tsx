
import React, { useContext, useEffect, useMemo, useReducer, useRef } from 'react';
import BasicAuthContext, { BasicAuthContextInterface, BasicAuthUser, initialAuthState } from './BasicAuthContext';
import { AuthStates, stateReducer } from './AuthStates';
import { clearData, getData, isRefreshTokenExpired, isTokenExpiredIn, storeData } from '@/utils/helpers/token';
import { get } from 'lodash';
import { ROUTES } from '@/utils/constants/route';
import message from 'antd/es/message';
import { useLogin } from '@/hooks/login';
import { ENV } from '@/constants';
import { getCurrentUser } from '@/services/user';
import { AuthUser } from '../types';
import { requestToken } from '../components/utils/request';

window.portalConfig = ENV

export interface BasicAuthenticateProps {
  children?: React.ReactNode;
  context?: React.Context<BasicAuthContextInterface>;
}

const BasicAuthProvider = (opts : BasicAuthenticateProps) => {

  const { mutateAsync: login } = useLogin();

  const {
    children,
    context = BasicAuthContext
  } = opts;

  const [state, dispatch] = useReducer(stateReducer, initialAuthState);
  const init = useRef(false);

  useEffect(() => {
    console.log("initialize user context");
    if (init.current) {
      return;
    }
    console.log("Checking auth status");
    const fetchUser = async () => {
      const curUser = (getCurrentUser())?.data;
      dispatch({
        type: AuthStates.LOGIN_COMPLETE,
        user: curUser,
      });
      window.location.href = `${window.location.origin}`;
    };

    init.current = true;
    if (checkAuth()) {
      window.portalConfig.getAccessToken = getAccessToken;
      console.log("Authenticated, Fetching user info...");

      let user : BasicAuthUser;
        let userStr = getData("user");
        if (userStr) {
          user = JSON.parse(userStr);
          console.log("Login data found")
          dispatch({
            type: AuthStates.LOGIN_COMPLETE,
            user: user,
          });
        } else {
          console.log("user info not found, fetching new...")
          fetchUser();
        }
    } else {
      console.log("Login data not found")
      dispatch({type: AuthStates.LOGOUT});
    }
  }, []);

  const checkAuthenticated = () : boolean => {
    return checkAuth();
  };

  const getCurrentAuthUser = () : any => {
    const user: AuthUser = JSON.parse(getData("user") ?? "{}");
    return user;
  };

  window.portalConfig.checkAuthenticated = checkAuthenticated;
  window.portalConfig.getCurrentAuthUser = getCurrentAuthUser;

  const getAccessToken = async () : Promise<string> => {
    const token = getData("token");
    const refreshToken = getData("refreshToken");
    if (!refreshToken || isRefreshTokenExpired() || !token) {
      clearData("token");
      clearData("tokenExpired");
      return Promise.resolve("Refresh token expired");
    }
    //whether be expired in 5 min
    if (!isTokenExpiredIn(5 * 60 * 1000 * 1000)) {
      return Promise.resolve(token);
    }
    try {
      console.log("requesting token...");
      const res = await requestToken(refreshToken);
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
      return Promise.resolve(nToken);
    } catch (e) {
      void message.error("Your session has expired. Please log in again.");
      clearData("token");
      clearData("tokenExpired");
      window.location.href = `${window.location.origin}${ROUTES.LOGIN}`;
      return Promise.reject(new Error(`Exception while request access token: ${e}`));
    }
  };

  const loginWithCredentials = async (values: any) : Promise<void> => {
    dispatch({ type: AuthStates.LOGIN_STARTED});

    const res = await login(values);
    const accessToken = get(res, "data.accessToken");
    const expiresIn = get(res, "data.expiresIn");
    const refreshToken = get(res, "data.refreshToken");
    const refreshTokenExpiresIn = get(res, "data.refreshTokenExpiresIn");

    if (accessToken && expiresIn) {
      storeData("token", accessToken);
      storeData("tokenExpired", String(Date.now() + expiresIn * 1000));
      if (refreshToken) {
        storeData("refreshToken", refreshToken);
      }
      if (refreshTokenExpiresIn) {
        storeData(
          "refreshTokenExpiresIn",
          String(Date.now() + refreshTokenExpiresIn * 1000));
      }

      window.portalConfig.getAccessToken = getAccessToken;
      window.portalConfig.checkAuthenticated = checkAuthenticated;
      window.portalConfig.getCurrentAuthUser = getCurrentAuthUser;
      const curUser = getCurrentUser();
      console.log("store currentUser: " + JSON.stringify(curUser))
      storeData("user", JSON.stringify(curUser));

      dispatch({
        type: AuthStates.LOGIN_COMPLETE,
        user: curUser,
      });
      window.location.href = `${window.location.origin}`;
    } else {
      throw new Error("Invalid username or password.");
    }
  };

  const refreshAuth = async () : Promise<void> => {
    const accessToken = getData("token");
    const expiresIn = getData("tokenExpired");
    const refreshToken = getData("refreshToken");
    const refreshTokenExpiresIn = getData("refreshTokenExpiresIn");
    if (accessToken && expiresIn && refreshToken && refreshTokenExpiresIn) {
      if (!isRefreshTokenExpired()) {
        console.log("refresh token");
        console.log("Logout...");
        console.log("current state: " + state);
        console.log("tokenExpired..." + expiresIn);
        console.log("refreshTokenExpiresIn..." + refreshTokenExpiresIn);
        window.portalConfig.getAccessToken = getAccessToken;
        window.portalConfig.checkAuthenticated = checkAuthenticated;
        window.portalConfig.getCurrentAuthUser = getCurrentAuthUser;
        const curUser = getCurrentUser();
        if (curUser) {
          console.log("store currentUser: " + JSON.stringify(curUser))
          storeData("user", JSON.stringify(curUser));
        }
      }
    }
  };

  const checkAuth = () : boolean => {
    const token = getData("token");
    const refreshToken = getData("refreshToken");
    if (!refreshToken || isRefreshTokenExpired() || !token) {
      clearData("user");
      clearData("token");
      clearData("tokenExpired");
      return false;
    }
    return true;
  };

  const logout = async () : Promise<void> => {
    console.log("Logout...");
    console.log("current state: " + state);
    console.log("tokenExpired..." + getData("tokenExpired"));
    console.log("refreshTokenExpiresIn..." + getData("refreshTokenExpiresIn"));
    dispatch({ type: AuthStates.LOGOUT});
    clearData("user");
    clearData("token");
    clearData("tokenExpired");
  };

  const contextValue = useMemo<BasicAuthContextInterface<BasicAuthUser>>(() => {
    return {
      ...state,
      loginWithCredentials,
      getAccessToken,
      logout,
      refreshAuth,
      checkAuthenticated,
      getCurrentAuthUser
    };
  }, [
    state,
    loginWithCredentials,
    getAccessToken,
    logout,
    refreshAuth,
    checkAuthenticated,
    getCurrentAuthUser
  ]);

  return (
    <context.Provider value={contextValue}>
      {children}
    </context.Provider>
  );
};

const useBasicAuth = <TUser extends BasicAuthUser = BasicAuthUser>(
  context = BasicAuthContext
): BasicAuthContextInterface<TUser> =>
  useContext(context) as BasicAuthContextInterface<TUser>;

export { BasicAuthProvider, useBasicAuth };