import { Tree } from "antd";
import { get, isEmpty } from "lodash";
import { useMemo } from "react";
import styles from "./index.module.scss";
import { schemaParses } from "@/utils/helpers/schema";
import Text from "@/components/Text";

type Props = {
  item: any;
  schemas: any;
};

const RequestBody = ({ item, schemas }: Props) => {
  const data = useMemo(() => {
    if (isEmpty(item?.content)) {
      return undefined;
    }
    const schemaUrl = get(
      item,
      `content.application/json.schema.$ref`,
      get(
        item,
        `.content.application/xml
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
  }, [item, schemas]);
  return isEmpty(data) ? (
    <></>
  ) : (
    <>
      <Text.LightLarge>Request body</Text.LightLarge>
      <div className={styles.tree}>
        <Tree treeData={data} />
      </div>
    </>
  );
};

export default RequestBody;
