import Text from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import {
  convertSchemaToTypeOnly,
  parseObjectDescriptionToTreeData,
} from "@/utils/helpers/schema";
import { Button, Collapse, Empty, Flex } from "antd";
import { useMemo, useState } from "react";
import { useCommonAddProp } from "../commonHook";
import styles from "./index.module.scss";
import { isEmpty } from "lodash";

interface Props {
  onSelect?: (prop: any) => void;
}

const RightAddSellerProp = ({ onSelect }: Readonly<Props>) => {
  const { sellerApi, rightSideInfo } = useNewApiMappingStore();
  const [selectedProp, setSelectedProp] = useState<any>({
    location: rightSideInfo?.previousData?.targetLocation,
    name: rightSideInfo?.previousData?.target,
  });
  const pathParameters = useMemo(() => {
    if (!sellerApi?.spec?.parameters) return [];
    return sellerApi?.spec?.parameters?.filter(
      (parameter: any) => parameter.in === "path"
    );
  }, [sellerApi]);

  const queryParameters = useMemo(() => {
    if (!sellerApi?.spec?.parameters) return [];
    return sellerApi?.spec?.parameters?.filter(
      (parameter: any) => parameter.in === "query"
    );
  }, [sellerApi]);

  const requestBodyTree = useMemo(() => {
    if (!sellerApi?.spec?.requestBody) return undefined;
    const contentType = Object.keys(sellerApi?.spec?.requestBody.content)?.[0];
    const properties =
      sellerApi?.spec.requestBody.content[contentType]?.schema?.properties;
    const simplifiedProperties = convertSchemaToTypeOnly(properties);
    return parseObjectDescriptionToTreeData(
      simplifiedProperties,
      styles.treeTitle,
      styles.treeValue
    );
  }, [sellerApi]);

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
    <Flex vertical gap={16} style={{ width: "100%", height: "100%" }}>
      <div className={styles.header}>
        <Text.NormalLarge lineHeight="24px">
          Select Seller API mapping property
        </Text.NormalLarge>
      </div>
      <div className={styles.container}>
        {!isEmpty(collapseItems) ? (
          <Collapse
            items={collapseItems}
            defaultActiveKey={["path", "query", "request"]}
            ghost
            className={styles.collapse}
            expandIconPosition="end"
          />
        ) : (
          <Empty
            description="No request property"
            style={{
              alignItems: "center",
              flexDirection: "column",
              display: "flex",
              justifyContent: "center",
              height: "100%",
            }}
          />
        )}
      </div>
      <Flex justify="flex-end" className={styles.footer}>
        <Button
          data-testid="seller-prop-ok"
          type="primary"
          onClick={handleAddProp}
          disabled={isEmpty(rightSideInfo)}
        >
          OK
        </Button>
      </Flex>
    </Flex>
  );
};

export default RightAddSellerProp;
