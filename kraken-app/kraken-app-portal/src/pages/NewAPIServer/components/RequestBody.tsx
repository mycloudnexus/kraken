import { Button, Tree } from "antd";
import { get, isEmpty } from "lodash";
import { useMemo } from "react";
import styles from "./index.module.scss";
import { exampleParse, schemaParses } from "@/utils/helpers/schema";
import { Text } from "@/components/Text";

type Props = {
  item: any;
  schemas: any;
  showTitle?: boolean;
};

const RequestBody = ({ item, schemas, showTitle = true }: Props) => {
  const data = useMemo(() => {
    if (isEmpty(item?.content)) {
      return undefined;
    }
    const objectKey = get(Object.keys(get(item, `content`, {})), "[0]", "");
    const example = get(
      item,
      `content[${objectKey}].examples.response.value`,
      get(
        item,
        `content[${objectKey}].example`,
        get(item, `content[${objectKey}].examples`)
      )
    );

    if (!isEmpty(example)) {
      return exampleParse(example, "", styles.nodeTitle, styles.nodeExample);
    }
    const schemaUrl = get(
      item,
      `content[${objectKey}].schema.items.$ref`,
      get(
        item,
        `content[${objectKey}].schema.$ref`,
        get(
          item,
          `content[${objectKey}]
        .schema.$ref`,
          ""
        )
      )
    );
    if (typeof schemaUrl !== "string") {
      return undefined;
    }
    const schemaProperties = get(
      item,
      `content[${objectKey}].schema.properties`
    );
    if (!isEmpty(schemaProperties)) {
      return exampleParse(
        schemaProperties,
        "",
        styles.nodeTitle,
        styles.nodeExample
      );
    }
    return schemaParses(
      schemaUrl.replace("#/components/schemas/", ""),
      schemas,
      "",
      styles.nodeTitle,
      styles.nodeExample
    );
  }, [item, schemas]);
  return isEmpty(data) ? (
    <></>
  ) : (
    <>
      {showTitle && <Text.LightLarge>Request body</Text.LightLarge>}
      <div style={{ marginTop: showTitle ? 0 : 12 }}>
        <Button
          style={{
            borderColor: "#1677ff",
            color: "#1677ff",
          }}
        >
          API spec
        </Button>
      </div>
      <div className={styles.tree}>
        <Tree treeData={data} />
      </div>
    </>
  );
};

export default RequestBody;
