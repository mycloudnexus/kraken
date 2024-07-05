import { useGetComponentList } from "@/hooks/product";
import { COMPONENT_KIND_API_TARGET_SPEC } from "@/utils/constants/product";
import { notification } from "antd";
import jsYaml from "js-yaml";
import { get } from "lodash";
import { useEffect, useMemo, useState } from "react";
import swaggerClient from "swagger-client";

export interface IServerKeyInfo {
  method: string;
  path: string;
  serverKey: string;
}
const useGetDefaultSellerApi = (
  currentProduct: string,
  serverKeyInfo: IServerKeyInfo
) => {
  const { data: dataList, isLoading } = useGetComponentList(currentProduct, {
    kind: COMPONENT_KIND_API_TARGET_SPEC,
    size: 1000,
  });

  const apiTargetSpecComponent = useMemo(() => {
    if (isLoading) return;

    return dataList?.data?.find(
      (component: any) => component.metadata.key === serverKeyInfo.serverKey
    );
  }, [dataList, serverKeyInfo, isLoading]);

  const baseSpec = useMemo(() => {
    try {
      const encoded = apiTargetSpecComponent?.facets?.baseSpec?.content;
      if (!encoded) return undefined;
      const yamlContent = atob(encoded.slice(31))
        .replace(/(â)/g, "")
        .replace(/(â)/g, "");
      const result = jsYaml.load(yamlContent);
      return result;
    } catch (error) {
      notification.error({
        message:
          "Can not read the info from your api spec file, please upload the correct one",
      });
      return undefined;
    }
  }, [apiTargetSpecComponent]);

  const [resolvedSpec, setResolvedSpec] = useState<any>();
  useEffect(() => {
    if (!baseSpec) return;
    (async () => {
      const result = await swaggerClient.resolve({ spec: baseSpec });
      setResolvedSpec(result.spec);
    })();
  }, [baseSpec]);

  const selectedSpec = useMemo(() => {
    if (isLoading || !resolvedSpec) return;
    const name = get(apiTargetSpecComponent, "metadata.name");
    const listSpec: any[] = [];
    Object.entries(resolvedSpec.paths).forEach(
      ([path, methodObj]: [string, any]) => {
        Object.entries(methodObj).forEach(([method, spec]) => {
          listSpec.push({
            name,
            url: path,
            method,
            spec,
          });
        });
      }
    );
    const selectedSpec = listSpec.find(
      (item) =>
        item.url === serverKeyInfo.path && item.method === serverKeyInfo.method
    );
    return selectedSpec;
  }, [isLoading, resolvedSpec, apiTargetSpecComponent, serverKeyInfo]);

  return {
    sellerApi: selectedSpec,
    serverKey: serverKeyInfo.serverKey,
  };
};

export default useGetDefaultSellerApi;
