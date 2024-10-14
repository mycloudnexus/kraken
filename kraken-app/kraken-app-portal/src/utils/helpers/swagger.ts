import { get, isEmpty } from "lodash";

export const transformApiData = (apiData: Record<string, any>) => {
  if (isEmpty(apiData)) {
    return [];
  }
  return Object.entries(apiData).flatMap(([path, pathData]) =>
    Object.entries(pathData).map(([method, methodData]) => ({
      title: get(methodData, "summary", ""),
      method: method.toLowerCase(),
      path,
      api: `${path} ${method.toLowerCase()}`,
    }))
  );
};
