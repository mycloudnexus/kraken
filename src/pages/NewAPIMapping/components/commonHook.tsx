import TypeTag from "@/components/TypeTag";
import { EnumRightType } from "@/utils/types/common.type";
import { CollapseProps, Flex, Tree, Typography, notification } from "antd";
import clsx from "clsx";
import { Dispatch, useCallback, useMemo } from "react";
import styles from "./RightAddSellerProp/index.module.scss";

interface Props {
  rightSide: EnumRightType;
  selectedProp: any;
  rightSideInfo: any;
  pathParameters: any;
  queryParameters: any;
  requestBodyTree: any;
  setSelectedProp: Dispatch<any>;
  onSelect?: (prop: any) => void;
}

export const useCommonAddProp = ({
  rightSide,
  selectedProp,
  rightSideInfo,
  pathParameters,
  queryParameters,
  requestBodyTree,
  setSelectedProp,
  onSelect,
}: Props) => {
  const handleAddProp = useCallback(() => {
    if (!selectedProp) {
      notification.error({ message: "Please select one property!" });
      return;
    }
    onSelect?.({ ...selectedProp, title: rightSideInfo?.title });
  }, [selectedProp, onSelect, rightSideInfo]);

  const collapseItems = useMemo(() => {
    const items: CollapseProps["items"] = [];
    if (pathParameters.length) {
      items.push({
        key: "path",
        label: (
          <Typography.Text className={styles.title}>
            Path parameters
          </Typography.Text>
        ),
        children: (
          <Flex vertical gap={8} className={styles.paramList}>
            {pathParameters.map((parameter: any) => (
              <Flex
                align="center"
                justify="space-between"
                className={clsx(styles.paramItem, {
                  [styles.active]:
                    selectedProp?.location === "PATH" &&
                    selectedProp?.name === parameter.name,
                })}
                key={parameter.name}
                onClick={() =>
                  setSelectedProp({
                    location: "PATH",
                    name: `@{{${parameter.name}}}`,
                  })
                }
              >
                {parameter.name} <TypeTag type={parameter.schema.type} />
              </Flex>
            ))}
          </Flex>
        ),
      });
    }
    if (queryParameters.length) {
      items.push({
        key: "query",
        label: (
          <Typography.Text className={styles.title}>
            Query parameters
          </Typography.Text>
        ),
        children: (
          <Flex vertical gap={8} className={styles.paramList}>
            {queryParameters.map((parameter: any) => (
              <Flex
                align="center"
                justify="space-between"
                className={clsx(styles.paramItem, {
                  [styles.active]:
                    selectedProp?.location === "QUERY" &&
                    selectedProp?.name === parameter.name,
                })}
                key={parameter.name}
                onClick={() =>
                  setSelectedProp({
                    location: "QUERY",
                    name: `@{{${parameter.name}}}`,
                  })
                }
              >
                {parameter.name}
                <TypeTag type={parameter.schema.type} />
              </Flex>
            ))}
          </Flex>
        ),
      });
    }
    if (requestBodyTree) {
      items.push({
        key: "request",
        label: (
          <Typography.Text className={styles.title}>
            Request body
          </Typography.Text>
        ),
        children: (
          <div className={styles.tree}>
            <Tree
              treeData={requestBodyTree}
              selectable
              selectedKeys={
                selectedProp?.location === "BODY"
                  ? [
                      selectedProp?.name
                        .replace("@{{", "")
                        .replace("}}", "")
                        .replace("requestBody.", "")
                        .replace("responseBody.", ""),
                    ]
                  : []
              }
              onSelect={(_, e) => {
                setSelectedProp({
                  location: "BODY",
                  name: `@{{${
                    rightSide === EnumRightType.AddSonataProp
                      ? "requestBody"
                      : "responseBody"
                  }.${e.node.key}}}`,
                });
              }}
            />
          </div>
        ),
      });
    }
    return items;
  }, [pathParameters, queryParameters, requestBodyTree, selectedProp]);

  return {
    handleAddProp,
    collapseItems,
  };
};
