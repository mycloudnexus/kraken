import { useGetComponentList } from "@/hooks/product";
import {
  COMPONENT_KIND_API,
  COMPONENT_KIND_API_SPEC,
  COMPONENT_KIND_API_TARGET,
  COMPONENT_KIND_API_TARGET_MAPPER,
} from "@/utils/constants/product";
import jsYaml from "js-yaml";
import { useMemo } from "react";

const useGetApiSpec = (currentProduct: string, query: string) => {
  const { data: mapperResponse } = useGetComponentList(currentProduct, {
    kind: COMPONENT_KIND_API_TARGET_MAPPER,
    q: query,
    facetIncluded: true,
    page: 0,
    size: 20,
  });
  const endpoint = mapperResponse?.data?.[0]?.facets?.endpoints?.[0];
  const mappers = endpoint?.mappers;

  const { data: targetResponse } = useGetComponentList(currentProduct, {
    kind: COMPONENT_KIND_API_TARGET,
    q: query,
    facetIncluded: true,
    page: 0,
    size: 20,
  });
  const metadataKey = targetResponse?.data?.[0]?.metadata?.key;

  const { data: apiComponentList } = useGetComponentList(currentProduct, {
    kind: COMPONENT_KIND_API,
  });
  const { data: apiSpecList } = useGetComponentList(currentProduct, {
    kind: COMPONENT_KIND_API_SPEC,
  });

  const apiSpecMetadataKey = useMemo(() => {
    if (!apiComponentList) return undefined;
    const component = apiComponentList?.data?.find((component: any) =>
      component.links.some((link: any) => link.targetAssetKey === metadataKey)
    );
    return component?.links?.find((link: any) =>
      link.relationship.includes("api-spec")
    )?.targetAssetKey;
  }, [apiComponentList, metadataKey]);
  const apiSpec = useMemo(() => {
    if (!apiSpecList || !apiSpecMetadataKey) return undefined;
    return apiSpecList?.data?.find(
      (apiSpec: any) => apiSpec?.metadata?.key === apiSpecMetadataKey
    );
  }, [apiSpecMetadataKey, apiSpecList]);

  const jsonSpec = useMemo(() => {
    if (!apiSpec) return undefined;
    const yamlContent = atob(apiSpec?.facets?.baseSpec?.content);
    return jsYaml.load(yamlContent);
  }, [apiSpec]);

  const serverKeyInfo = {
    method: endpoint?.method,
    path: endpoint?.path,
    serverKey: endpoint?.serverKey,
  };

  return {
    mapperResponse,
    serverKeyInfo,
    mappers,
    jsonSpec,
  };
};

export default useGetApiSpec;
