import Flex from "@/components/Flex";
import { Button, Empty, Tree } from "antd";
import { get, isEmpty } from "lodash";
import { useEffect, useMemo, useState } from "react";
import styles from "./index.module.scss";
import { schemaParses } from "@/utils/helpers/schema";

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
    const schemaUrl = get(
      item,
      `${selectedResponse}.content.application/json.schema.$ref`,
      get(
        item,
        `${selectedResponse}.content.application/xml
      .schema.$ref`,
        ""
      )
    ).replace("#/components/schemas/", "");
    return schemaParses(
      schemaUrl,
      schemas,
      "",
      styles.nodeTitle,
      styles.nodeExample
    );
  }, [selectedResponse, item, schemas]);

  return (
    <div>
      <Flex justifyContent="flex-start" flexWrap="wrap">
        {responseKeys?.map((rKey: string) => (
          <Button
            key={rKey}
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
