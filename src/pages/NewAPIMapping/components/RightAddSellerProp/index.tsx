import Text from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import {
  convertSchemaToTypeOnly,
  parseObjectDescriptionToTreeData,
} from "@/utils/helpers/schema";
import { Button, Collapse, Flex } from "antd";
import { useMemo, useState } from "react";
import { useCommonAddProp } from "../commonHook";
import styles from "./index.module.scss";

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
    <Flex vertical gap={16} style={{ width: "100%", minHeight: "100%" }}>
      <Text.BoldLarge>
        Select mapping property from Seller API request
      </Text.BoldLarge>
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

export default RightAddSellerProp;
