/**
 * store the token, using localstorage
 * @param token
 */
export const storeData = (key: string, token: string): string => {
  window.localStorage.setItem(key, token);
  return token;
};

/**
 * clear the token
 */
export const clearData = (key: string) => {
  window.localStorage.removeItem(key);
};

/**
 * get the token, from widow.appToken or localstorage
 */
export const getData = (key: string) => {
  if (typeof window !== "undefined") {
    return window.localStorage.getItem(key);
  }
};

export const isTokenExpired = () => {
  const storedTime = getData("tokenExpired");
  const token = getData("token");
  if (!storedTime || !token) {
    return true;
  }
  const currentTime = Date.now();
  return currentTime > Number(storedTime);
};

export const isTokenExpiredIn = (reserved : number) => {
  const storedTime = getData("tokenExpired");
  const token = getData("token");
  if (!storedTime || !token) {
    return true;
  }
  const currentTime = Date.now();
  return currentTime > Number(storedTime) - reserved;
};

export const isRefreshTokenExpired = () => {
  const storedTime = getData("refreshTokenExpiresIn");
  const refreshToken = getData("refreshToken");
  if (!storedTime || !refreshToken) {
    return true;
  }
  const currentTime = Date.now();
  return currentTime > Number(storedTime);
};
