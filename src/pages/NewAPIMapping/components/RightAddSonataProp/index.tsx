import Text from "@/components/Text";
import TypeTag from "@/components/TypeTag";
import {
  convertSchemaToTypeOnly,
  parseObjectDescriptionToTreeData,
} from "@/utils/helpers/schema";
import { Checkbox, Collapse, CollapseProps, Flex, Tree } from "antd";
import { merge } from "lodash";
import { useEffect, useMemo, useState } from "react";
import swaggerClient from "swagger-client";
import styles from "./index.module.scss";

interface Props {
  spec: any;
  method: string;
}

const RightAddSonataProp = ({ spec, method }: Readonly<Props>) => {
  const [resolvedSpec, setResolvedSpec] = useState<any>();
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

  const collapseItems = useMemo(() => {
    const items: CollapseProps["items"] = [];
    if (pathParameters.length) {
      items.push({
        key: "path",
        label: "Path parameters",
        children: (
          <>
            {pathParameters.map((parameter: any) => (
              <Checkbox value={parameter.name} key={parameter.name}>
                <TypeTag type={parameter.schema.type} /> {parameter.name}
              </Checkbox>
            ))}
          </>
        ),
      });
    }
    if (queryParameters.length) {
      items.push({
        key: "query",
        label: "Query parameters",
        children: (
          <>
            {queryParameters.map((parameter: any) => (
              <Checkbox value={parameter.name} key={parameter.name}>
                <TypeTag type={parameter.schema.type} /> {parameter.name}
              </Checkbox>
            ))}
          </>
        ),
      });
    }
    if (requestBodyTree) {
      items.push({
        key: "request",
        label: "Request body parameters",
        children: (
          <div className={styles.tree}>
            <Tree treeData={requestBodyTree} selectable />
          </div>
        ),
      });
    }
    return items;
  }, [pathParameters, queryParameters, requestBodyTree]);
  return (
    <Flex vertical gap={16} style={{ width: "100%" }}>
      <Text.BoldLarge>Add mapping property from Sonata API</Text.BoldLarge>
      <Collapse ghost items={collapseItems} className={styles.collapse} />
    </Flex>
  );
};

export default RightAddSonataProp;
