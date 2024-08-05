import { useGetComponentList, useGetComponentListV2 } from "@/hooks/product";
import {
  COMPONENT_KIND_API,
  COMPONENT_KIND_API_SPEC,
} from "@/utils/constants/product";
import { extractOpenApiStrings } from "@/utils/helpers/schema";
import transformTarget from "@/utils/helpers/transformTarget";
import { decode } from "js-base64";
import jsYaml from "js-yaml";
import { useMemo, useCallback } from "react";

const useGetApiSpec = (currentProduct: string, targetMapperKey: string) => {
  const {
    data: mapperResponse,
    isLoading,
    refetch,
  } = useGetComponentListV2(currentProduct, targetMapperKey);
  const endpoint = mapperResponse?.facets?.endpoints?.[0];
  const mappers = endpoint?.mappers;
  const metadataKey = mapperResponse?.metadata?.key;
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
    const yamlContent = extractOpenApiStrings(
      decode(apiSpec?.facets?.baseSpec?.content)
    );
    return jsYaml.load(yamlContent);
  }, [apiSpec]);

  const serverKeyInfo = {
    method: endpoint?.method,
    path: endpoint?.path,
    serverKey: endpoint?.serverKey,
  };
  const resetMapping = useCallback(() => {
    return mappers?.request?.map((rm: any) => ({
      ...rm,
      target: transformTarget(rm.target, rm.targetLocation),
      source: transformTarget(rm.source, rm.sourceLocation),
    }));
  }, [mappers?.request]);

  return {
    mapperResponse,
    serverKeyInfo,
    mappers,
    jsonSpec,
    loadingMapper: isLoading,
    metadataKey,
    resetMapping,
    refreshMappingDetail: refetch,
  };
};

export default useGetApiSpec;
