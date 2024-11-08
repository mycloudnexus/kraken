import ExampleValueModal from "@/components/ExampleValueModal";
import { Text } from "@/components/Text";
import TypeTag from "@/components/TypeTag";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import {
  Button,
  CollapseProps,
  Flex,
  Tree,
  Typography,
  notification,
} from "antd";
import classNames from "classnames";
import clsx from "clsx";
import { get, isEmpty, omit } from "lodash";
import { Dispatch, useCallback, useMemo, useState } from "react";
import { useBoolean } from "usehooks-ts";
import styles from "./RightAddSellerProp/index.module.scss";

interface Props {
  selectedProp: any;
  rightSideInfo: any;
  pathParameters: any;
  queryParameters: any;
  requestBodyTree: any;
  setSelectedProp: Dispatch<any>;
  onSelect?: (prop: any) => void;
}

export const useCommonAddProp = ({
  selectedProp,
  rightSideInfo,
  pathParameters,
  queryParameters,
  requestBodyTree,
  setSelectedProp,
  onSelect,
}: Props) => {
  const { sellerAPIExampleProps, setSellerAPIExampleProps } =
    useNewApiMappingStore();
  const [currentProp, setCurrentProp] = useState<Record<string, string>>();
  const { value: isOpen, setTrue: open, setFalse: close } = useBoolean(false);

  const handleAddProp = useCallback(() => {
    if (!selectedProp) {
      notification.error({ message: "Please select one property!" });
      return;
    }
    // selectedProp = { name: string, location: string }
    // propery name should follow this format: @{{ prop_name }}
    onSelect?.({
      ...selectedProp,
      name: `@{{${selectedProp.name}}}`,
      title: rightSideInfo?.title,
    });
  }, [selectedProp, onSelect, rightSideInfo]);

  const selectedKey = useMemo(
    () => get(selectedProp, "name", ""),
    [selectedProp]
  );

  const handleProp = (name: string, path: string) => {
    setCurrentProp({
      name,
      path,
    });
    open();
  };

  const setHybridField = (
    field: keyof typeof sellerAPIExampleProps,
    value: string
  ) => {
    if (isEmpty(value)) {
      setSellerAPIExampleProps({
        ...sellerAPIExampleProps,
        [field]: omit(sellerAPIExampleProps?.[field], [
          `${get(currentProp, "name", "")}`,
        ]),
      });
      setSelectedProp({
        location: "",
        name: "",
      });
      close();
      return;
    }
    setSellerAPIExampleProps({
      ...sellerAPIExampleProps,
      [field]: {
        ...sellerAPIExampleProps?.[field],
        [get(currentProp, "name", "")]: value,
      },
    });
    close();
    setSelectedProp({
      location: "HYBRID",
      name: value,
    });
  };

  const handleAddPathHybrid = (value: string) => {
    setHybridField("path", value);
  };

  const handleAddParamHybrid = (value: string) => {
    setHybridField("param", value);
  };

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
          <>
            {isOpen && currentProp?.path === "PATH" && (
              <ExampleValueModal
                location={currentProp?.path || ""}
                attribute={currentProp?.name || ""}
                isOpen={isOpen}
                onClose={close}
                onOK={handleAddPathHybrid}
              />
            )}
            {pathParameters.map((parameter: any) => (
              <Flex
                vertical
                gap={8}
                className={styles.paramList}
                key={parameter.name}
              >
                <Flex
                  align="center"
                  justify="space-between"
                  className={clsx(styles.paramItem, {
                    [styles.active]:
                      (selectedProp?.location === "PATH" &&
                        selectedProp?.name === parameter.name) ||
                      (selectedProp?.name ===
                        sellerAPIExampleProps?.path?.[parameter.name] &&
                        selectedProp?.location === "HYBRID"),
                  })}
                  key={parameter.name}
                  onClick={() => {
                    if (sellerAPIExampleProps?.path?.[parameter.name]) {
                      setSelectedProp({
                        location: "HYBRID",
                        name: sellerAPIExampleProps?.path?.[parameter.name],
                      });
                      return;
                    }
                    setSelectedProp({
                      location: "PATH",
                      name: parameter.name,
                    });
                  }}
                >
                  <Text.LightMedium ellipsis>{parameter.name}</Text.LightMedium>
                  <Flex justify="flex-end" align="center">
                    {parameter?.schema?.type?.toLowerCase?.() === "object" ? (
                      <Button
                        type="link"
                        onClick={() => handleProp(parameter.name, "PATH")}
                      >
                        {sellerAPIExampleProps?.path?.[parameter.name]
                          ? "Edit value with variable"
                          : "Add value with variable"}
                      </Button>
                    ) : null}
                    <TypeTag type={parameter.schema.type} />
                  </Flex>
                </Flex>
                {sellerAPIExampleProps?.path?.[parameter.name] && (
                  <Typography.Text
                    ellipsis={{
                      tooltip: sellerAPIExampleProps?.path?.[parameter.name],
                    }}
                  >
                    {sellerAPIExampleProps?.path?.[parameter.name]}
                  </Typography.Text>
                )}
              </Flex>
            ))}
          </>
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
            {isOpen && currentProp?.path === "QUERY" && (
              <ExampleValueModal
                location={currentProp?.path || ""}
                attribute={currentProp?.name || ""}
                isOpen={isOpen}
                onClose={close}
                onOK={handleAddParamHybrid}
              />
            )}
            {queryParameters.map((parameter: any) => (
              <Flex vertical gap={8} key={parameter.name}>
                <Flex
                  align="center"
                  justify="space-between"
                  className={clsx(styles.paramItem, {
                    [styles.active]:
                      (selectedProp?.location === "QUERY" &&
                        selectedProp?.name === parameter.name) ||
                      (selectedProp?.name ===
                        sellerAPIExampleProps?.param?.[parameter.name] &&
                        selectedProp?.location === "HYBRID"),
                  })}
                  onClick={() => {
                    if (sellerAPIExampleProps?.param?.[parameter.name]) {
                      setSelectedProp({
                        location: "HYBRID",
                        name: sellerAPIExampleProps?.param?.[parameter.name],
                      });
                      return;
                    }
                    setSelectedProp({
                      location: "QUERY",
                      name: parameter.name,
                    });
                  }}
                >
                  <Text.LightMedium ellipsis>{parameter.name}</Text.LightMedium>
                  <Flex justify="flex-end" align="center">
                    {parameter?.schema?.type?.toLowerCase?.() === "object" ? (
                      <Button
                        type="link"
                        onClick={() => handleProp(parameter.name, "QUERY")}
                      >
                        {sellerAPIExampleProps?.param?.[parameter.name]
                          ? "Edit value with variable"
                          : "Add value with variable"}
                      </Button>
                    ) : null}
                    <TypeTag type={parameter.schema.type} />
                  </Flex>
                </Flex>
                {sellerAPIExampleProps?.param?.[parameter.name] && (
                  <Typography.Text
                    ellipsis={{
                      tooltip: sellerAPIExampleProps?.param?.[parameter.name],
                    }}
                  >
                    {sellerAPIExampleProps?.param?.[parameter.name]}
                  </Typography.Text>
                )}
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
        className: classNames(styles.flexOne, styles.requestBodySection),
        children: (
          <div className={styles.tree}>
            <Tree
              blockNode
              treeData={requestBodyTree}
              selectable
              selectedKeys={
                selectedProp?.location === "BODY" ? [selectedKey] : []
              }
              onSelect={(_, e) => {
                setSelectedProp({
                  location: "BODY",
                  name: e.node.key,
                });
              }}
            />
          </div>
        ),
      });
    }
    return items;
  }, [
    pathParameters,
    queryParameters,
    requestBodyTree,
    selectedProp,
    selectedKey,
    currentProp,
    isOpen,
    sellerAPIExampleProps,
    handleAddPathHybrid,
    handleAddParamHybrid,
  ]);

  return {
    handleAddProp,
    collapseItems,
  };
};
