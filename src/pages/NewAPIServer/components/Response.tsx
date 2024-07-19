import Flex from "@/components/Flex";
import { Button, Empty, Tree } from "antd";
import { get, isEmpty } from "lodash";
import { useEffect, useMemo, useState } from "react";
import styles from "./index.module.scss";
import {
  convertSchemaToTypeOnly,
  exampleParse,
  parseObjectDescriptionToTreeData,
  schemaParses,
} from "@/utils/helpers/schema";

type Props = {
  item: Record<string, any>;
  schemas: any;
};

const Response = ({ item, schemas }: Props) => {
  const [selectedResponse, setSelectedResponse] = useState<string>("");
  const responseKeys = useMemo(() => Object.keys(item || {}), [item]);
  useEffect(() => {
    if (isEmpty(responseKeys)) {
      return;
    }
    setSelectedResponse(responseKeys[0]);
  }, [responseKeys]);

  const currentSchema = useMemo(() => {
    if (!selectedResponse || isEmpty(item) || isEmpty(schemas)) {
      return undefined;
    }
    const objectKey = get(
      Object.keys(get(item, `${selectedResponse}.content`, {})),
      "[0]",
      ""
    );
    const example = get(
      item,
      `[${selectedResponse}].content[${objectKey}].examples.response.value`,
      get(
        item,
        `[${selectedResponse}].content[${objectKey}].example`,
        get(item, `[${selectedResponse}].content[${objectKey}].examples`)
      )
    );
    if (!isEmpty(example)) {
      return exampleParse(example, "", styles.nodeTitle, styles.nodeExample);
    }
    const properties = get(
      item,
      `[${selectedResponse}].content[${objectKey}].schema.items.properties`,
      get(
        item,
        `[${selectedResponse}].content[${objectKey}].schema.properties`,
        {}
      )
    );
    if (!isEmpty(properties)) {
      const simplifiedProperties = convertSchemaToTypeOnly(properties);
      return parseObjectDescriptionToTreeData(
        simplifiedProperties,
        styles.nodeTitle,
        styles.nodeExample
      );
    }
    const schemaUrl = get(
      item,
      `[${selectedResponse}].content[${objectKey}].schema.items.$ref`,
      get(
        item,
        `[${selectedResponse}].content[${objectKey}].schema.$ref`,
        get(
          item,
          `${selectedResponse}.content[${objectKey}]
              .schema.$ref`,
          ""
        )
      )
    );
    if (typeof schemaUrl !== "string") {
      return undefined;
    }
    return schemaParses(
      schemaUrl.replace("#/components/schemas/", ""),
      schemas,
      "",
      styles.nodeTitle,
      styles.nodeExample
    );
  }, [selectedResponse, item, schemas]);

  return (
    <div>
      <Flex justifyContent="flex-start" flexWrap="wrap">
        {responseKeys?.map((rKey: string, index: number) => (
          <Button
            key={rKey + index}
            style={
              selectedResponse === rKey
                ? {
                    borderColor: "#1677ff",
                    color: "#1677ff",
                  }
                : {}
            }
            onClick={() => setSelectedResponse(rKey)}
          >
            {rKey}
          </Button>
        ))}
      </Flex>
      <div className={styles.tree}>
        {!isEmpty(currentSchema) ? (
          <Tree treeData={currentSchema} />
        ) : (
          <Empty />
        )}
      </div>
    </div>
  );
};

export default Response;
