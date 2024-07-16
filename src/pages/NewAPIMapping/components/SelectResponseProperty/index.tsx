import Flex from "@/components/Flex";
import Text from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
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
import TitleIcon from "@/assets/title-icon.svg";

const Search = Input.Search;

const SelectResponseProperty = () => {
  const {
    sellerApi,
    activeResponseName,
    responseMapping,
    setResponseMapping,
    setActiveResponseName,
  } = useNewApiMappingStore();
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

  const handleOK = () => {
    const key = get(selectedKeys, "[0]");
    if (activeResponseName && typeof key === "string") {
      const [name, target] = activeResponseName.split("-");
      const cloneObj = clone(responseMapping);
      const index = cloneObj.findIndex((i: any) => {
        if (target !== "undefined") {
          return i.name === name && i.target === target;
        }
        return i.name === name;
      });
      set(cloneObj, `[${index}].source`, `@{{responseBody.${key}}}`);
      set(cloneObj, `[${index}].sourceLocation`, `BODY`);
      setResponseMapping(cloneObj);
      setActiveResponseName(undefined);
      setSelectedKeys([]);
    }
  };

  const newTreeData = findMatchingElements(dataTree, searchValue);

  return (
    <div className={styles.root}>
      <div className={styles.header}>
        <Text.BoldLarge>
          Select response property from seller API
        </Text.BoldLarge>
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
          <Flex justifyContent="space-between" gap={8}>
            <Flex justifyContent="flex-start" gap={8}>
              <TitleIcon />
              <Text.NormalMedium>Response body</Text.NormalMedium>
            </Flex>
            {isOpen ? (
              <DownOutlined onClick={toggleOpen} style={{ fontSize: 10 }} />
            ) : (
              <RightOutlined onClick={toggleOpen} style={{ fontSize: 10 }} />
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
            <div style={{ marginTop: 4 }}>
              <div className={styles.tree}>
                <Tree
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
            </div>
          </>
        )}
      </div>
      <Flex justifyContent="flex-end" className={styles.footer}>
        <Button
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
