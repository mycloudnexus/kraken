import Text from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import {
  convertSchemaToTypeOnly,
  parseObjectDescriptionToTreeData,
} from "@/utils/helpers/schema";
import { Button, Collapse, Flex } from "antd";
import { merge } from "lodash";
import { useEffect, useMemo, useState } from "react";
import swaggerClient from "swagger-client";
import { useCommonAddProp } from "../commonHook";
import styles from "./index.module.scss";

interface Props {
  spec: any;
  method: string;
  defaultProp?: {
    location: string;
    name: string;
  };
  onSelect?: (prop: any) => void;
}

const RightAddSonataProp = ({
  spec,
  method,
  defaultProp,
  onSelect,
}: Readonly<Props>) => {
  const { rightSideInfo } = useNewApiMappingStore();
  const [resolvedSpec, setResolvedSpec] = useState<any>();
  const [selectedProp, setSelectedProp] = useState<any>(defaultProp);
  useEffect(() => {
    if (!spec) return;
    (async () => {
      const result = await swaggerClient.resolve({ spec });
      setResolvedSpec(result.spec);
    })();
  }, [spec]);

  const correctSpec: any = useMemo(() => {
    if (!resolvedSpec) return;
    const paths = Object.values(resolvedSpec.paths);
    const flattenPathObject = {};
    merge(flattenPathObject, ...paths);
    return Object.entries(flattenPathObject).find(
      ([specMethod]) => method === specMethod
    )?.[1];
  }, [resolvedSpec, method]);

  const pathParameters = useMemo(() => {
    if (!correctSpec?.parameters) return [];
    return correctSpec?.parameters?.filter(
      (parameter: any) => parameter.in === "path"
    );
  }, [correctSpec]);

  const queryParameters = useMemo(() => {
    if (!correctSpec?.parameters) return [];
    return correctSpec?.parameters?.filter(
      (parameter: any) => parameter.in === "query"
    );
  }, [correctSpec]);

  const requestBodyTree = useMemo(() => {
    if (!correctSpec?.requestBody) return undefined;
    const contentType = Object.keys(correctSpec.requestBody.content)?.[0];
    const properties =
      correctSpec.requestBody.content[contentType]?.schema?.properties;
    const simplifiedProperties = convertSchemaToTypeOnly(properties);
    return parseObjectDescriptionToTreeData(
      simplifiedProperties,
      styles.treeTitle,
      styles.treeValue
    );
  }, [correctSpec]);

  const { handleAddProp, collapseItems } = useCommonAddProp(
    selectedProp,
    rightSideInfo,
    pathParameters,
    queryParameters,
    requestBodyTree,
    setSelectedProp,
    onSelect
  );

  return (
    <Flex vertical gap={16} style={{ width: "100%", minHeight: "100%" }}>
      <Text.BoldLarge>Add mapping property from Sonata API</Text.BoldLarge>
      <Collapse
        ghost
        items={collapseItems}
        defaultActiveKey={["path", "query", "request"]}
        className={styles.collapse}
        expandIconPosition="end"
      />
      <Flex justify="flex-end">
        <Button type="primary" onClick={handleAddProp}>
          OK
        </Button>
      </Flex>
    </Flex>
  );
};

export default RightAddSonataProp;
