import { useGetComponentList } from "@/hooks/product";
import {
  COMPONENT_KIND_API,
  COMPONENT_KIND_API_SPEC,
  COMPONENT_KIND_API_TARGET,
} from "@/utils/constants/product";
import jsYaml from "js-yaml";
import { useMemo } from "react";

const useGetApiSpec = (currentProduct: string, query: string) => {
  const { data: mappingFile } = useGetComponentList(currentProduct, {
    kind: COMPONENT_KIND_API_TARGET,
    q: query,
    facetIncluded: true,
    page: 0,
    size: 20,
  });
  const metadataKey = mappingFile?.data?.[0]?.metadata?.key;

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
  return {
    jsonSpec,
  };
};

export default useGetApiSpec;
