import { Text } from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import {
  convertSchemaToTypeOnly,
  parseObjectDescriptionToTreeData,
} from "@/utils/helpers/schema";
import { Button, Collapse, Empty, Flex } from "antd";
import { useEffect, useMemo, useState } from "react";
import swaggerClient from "swagger-client";
import { useCommonAddProp } from "../commonHook";
import styles from "./index.module.scss";
import { isEmpty } from "lodash";

export const getCorrectSpec = (spec: any, method: string) => {
  if (!spec) return;
  const flattenPathObject: any[] = [];

  for (const path in spec.paths) {
    const objSpecByMethod = spec.paths[path];
    for (const method in objSpecByMethod) {
      const singleSpec = objSpecByMethod[method];
      flattenPathObject.push({
        method,
        path,
        spec: singleSpec,
      });
    }
  }
  return flattenPathObject.find(
    (specWithMetadata) => method === specWithMetadata.method
  )?.spec;
};
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

  const correctSpec: any = useMemo(
    () => getCorrectSpec(resolvedSpec, method),
    [resolvedSpec, method]
  );

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

  const { handleAddProp, collapseItems } = useCommonAddProp({
    selectedProp,
    rightSideInfo,
    pathParameters,
    queryParameters,
    requestBodyTree,
    setSelectedProp,
    onSelect,
  });

  return (
    <Flex
      vertical
      gap={16}
      style={{ width: "100%", height: "100%" }}
      className={styles.root}
    >
      <div className={styles.header}>
        <Text.Custom size="15px" bold="500">
          Select Sonata API mapping property
        </Text.Custom>
      </div>
      <div className={styles.container}>
        {isEmpty(collapseItems) ? (
          <Empty
            description="No request property"
            style={{
              height: "100%",
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              flexDirection: "column",
            }}
          />
        ) : (
          <Collapse
            ghost
            items={collapseItems}
            defaultActiveKey={["path", "query", "request"]}
            className={styles.collapse}
            expandIconPosition="end"
          />
        )}
      </div>
      <Flex justify="flex-end" className={styles.footer}>
        <Button type="primary" onClick={handleAddProp} disabled={!selectedProp}>
          OK
        </Button>
      </Flex>
    </Flex>
  );
};

export default RightAddSonataProp;
