import TitleIcon from "@/assets/title-icon.svg";
import Flex from "@/components/Flex";
import { Text } from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { isElementInViewport } from "@/utils/helpers/html";
import {
  convertSchemaToTypeOnly,
  exampleParse,
  parseObjectDescriptionToTreeData,
} from "@/utils/helpers/schema";
import { DownOutlined, RightOutlined } from "@ant-design/icons";
import { Button, Input, Tree, notification } from "antd";
import { clone, get, isEmpty, set } from "lodash";
import { Key, useCallback, useMemo, useState } from "react";
import { useBoolean } from "usehooks-ts";
import styles from "./index.module.scss";

const Search = Input.Search;

const SelectResponseProperty = () => {
  const { sellerApi, activeResponseName, responseMapping, setResponseMapping } =
    useNewApiMappingStore();
  const { value: isOpen, toggle: toggleOpen } = useBoolean(true);
  const [searchValue, setSearchValue] = useState("");
  const [selectedKeys, setSelectedKeys] = useState<Key[]>([]);

  const dataTree = useMemo(() => {
    if (isEmpty(sellerApi)) return [];
    const response = get(
      sellerApi,
      `spec.responses.200`,
      get(sellerApi, `spec.responses.201`, {})
    );
    if (
      isEmpty(response) ||
      isEmpty(response.content) ||
      isEmpty(Object.keys(response?.content))
    ) {
      return [];
    }

    const contentType = get(Object.keys(response.content), "[0]");

    const example = get(
      response,
      `content[${contentType}].examples.response.value`,
      get(
        response,
        `content[${contentType}].example`,
        get(response, `content[${contentType}].examples`)
      )
    );

    if (!isEmpty(example)) {
      const exampleKeys = get(Object.keys(example), "[0]", "");
      const firstExample = get(example, `${exampleKeys}.value`);
      if (!isEmpty(firstExample)) {
        return exampleParse(
          firstExample,
          "",
          styles.nodeTitle,
          styles.nodeExample
        );
      }
      return exampleParse(example, "", styles.nodeTitle, styles.nodeExample);
    }

    const properties = get(
      response,
      `content[${contentType}].schema.items.properties`,
      get(response, `content[${contentType}].schema.properties`, {})
    );

    if (isEmpty(properties)) {
      return [];
    }
    const simplifiedProperties = convertSchemaToTypeOnly(properties);
    return parseObjectDescriptionToTreeData(
      simplifiedProperties,
      styles.nodeTitle,
      styles.nodeExample
    );
  }, [sellerApi]);

  const findMatchingElements = useCallback((data: any, search: string): any => {
    if (!search) {
      return data;
    }
    return data?.filter((i: any) => {
      if (i.key?.includes(search)) {
        return true;
      }
      if (!isEmpty(i.children)) {
        return !isEmpty(findMatchingElements(i.children, search));
      }
      return false;
    });
  }, []);

  const transformString = (input: string) => {
    if (input.indexOf("[*]") === -1) {
      return input;
    }
    const parts = input
      .replaceAll("0[*]", "[*]")
      .replaceAll("[*]", "[0]")
      .split(".");
    const newString = parts.join(".").replace("[0]", "[*]");
    return newString;
  };

  const handleOK = () => {
    const key = get(selectedKeys, "[0]");
    const newKey = transformString(key as unknown as string);
    if (activeResponseName && typeof key === "string") {
      const [responseIndex, name] = activeResponseName.split("-");
      if (
        !responseIndex ||
        get(responseMapping, `[${[responseIndex]}].name`, "undefined") !== name
      ) {
        notification.error({ message: "Error. Please try again" });
        return;
      }
      const cloneObj = clone(responseMapping);

      const value =
        newKey.startsWith("[*]") || newKey.startsWith("[0]")
          ? `@{{responseBody${newKey}}}`
          : `@{{responseBody.${newKey}}}`;
      set(cloneObj, `[${responseIndex}].source`, value);
      set(cloneObj, `[${responseIndex}].sourceLocation`, `BODY`);
      setResponseMapping(cloneObj);
      setSelectedKeys([]);
      const currentDom = document.getElementById(activeResponseName);
      if (currentDom && !isElementInViewport(currentDom, 210)) {
        currentDom?.scrollIntoView({ block: "center" });
      }
    }
  };

  const newTreeData = findMatchingElements(dataTree, searchValue);

  return (
    <div className={styles.root}>
      <div className={styles.header}>
        <Text.NormalLarge>Select Seller API response property</Text.NormalLarge>
      </div>
      <div className={styles.container}>
        <div>
          <Search
            placeholder="input search text"
            style={{ width: "100%", marginBottom: 8 }}
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
          />
        </div>
        <div style={{ marginTop: 16 }}>
          <Flex
            justifyContent="space-between"
            gap={8}
            onClick={toggleOpen}
            role="none"
            style={{ cursor: "pointer" }}
          >
            <Flex justifyContent="flex-start" gap={8}>
              <TitleIcon />
              <Text.NormalMedium>Response body</Text.NormalMedium>
            </Flex>
            {isOpen ? (
              <DownOutlined style={{ fontSize: 10 }} />
            ) : (
              <RightOutlined style={{ fontSize: 10 }} />
            )}
          </Flex>
        </div>
        {isOpen && (
          <>
            <div style={{ marginTop: 12 }}>
              <Button style={{ borderColor: "#2962FF", color: "#2962FF" }}>
                200
              </Button>
            </div>

            <div className={styles.tree}>
              <Tree
                data-testid="tree-item"
                selectedKeys={selectedKeys}
                treeData={newTreeData}
                selectable
                onSelect={(keys: Key[]) => {
                  if (!activeResponseName) {
                    notification.warning({
                      message:
                        "Please select property from Seller API response first",
                    });
                    return;
                  }
                  const mainKey: any = get(keys, "[0]");
                  setSelectedKeys([mainKey]);
                }}
              />
            </div>
          </>
        )}
      </div>
      <Flex justifyContent="flex-end" className={styles.footer}>
        <Button
          data-testid="ok-btn"
          onClick={handleOK}
          type="primary"
          disabled={isEmpty(selectedKeys)}
        >
          OK
        </Button>
      </Flex>
    </div>
  );
};

export default SelectResponseProperty;
