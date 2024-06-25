import Flex from "@/components/Flex";
import Text from "@/components/Text";
import { DownOutlined, RightOutlined } from "@ant-design/icons";
import { Button, Input, Tag, Tree } from "antd";
import { useBoolean } from "usehooks-ts";
import styles from "./index.module.scss";
import { Key, useCallback, useMemo, useState } from "react";
import { clone, get, isEmpty, set } from "lodash";

import {
  convertSchemaToTypeOnly,
  exampleParse,
  parseObjectDescriptionToTreeData,
} from "@/utils/helpers/schema";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";

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

  const dataTree = useMemo(() => {
    if (isEmpty(sellerApi)) return [];
    const response = get(
      sellerApi,
      `spec.responses.200`,
      get(sellerApi, `spec.responses.201`, {})
    );
    if (isEmpty(response)) {
      return [];
    }
    const contentType = get(Object.keys(response.content), "[0]");
    const example = get(
      response,
      `content[${contentType}].examples.response.value[0]`,
      get(
        response,
        `content[${contentType}].examples.response.value`,
        get(
          response,
          `content[${contentType}].example`,
          get(response, `content[${contentType}].examples`)
        )
      )
    );
    if (!isEmpty(example)) {
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

  const newTreeData = findMatchingElements(dataTree, searchValue);
  return (
    <div>
      <Text.BoldLarge>Select response property</Text.BoldLarge>
      <div style={{ marginTop: 16 }}>
        <Search
          placeholder="input search text"
          style={{ width: "80%", marginBottom: 8 }}
          value={searchValue}
          onChange={(e) => setSearchValue(e.target.value)}
        />
        <Flex justifyContent="flex-start" gap={2}>
          <Tag color="green">String</Tag>
          <Tag color="blue">JSON</Tag>
        </Flex>
      </div>
      <div style={{ marginTop: 16 }}>
        <Flex justifyContent="flex-start" gap={8}>
          {isOpen ? (
            <DownOutlined onClick={toggleOpen} style={{ fontSize: 10 }} />
          ) : (
            <RightOutlined onClick={toggleOpen} style={{ fontSize: 10 }} />
          )}
          <Text.LightMedium>Responses</Text.LightMedium>
        </Flex>
      </div>
      {isOpen && (
        <>
          <div style={{ marginTop: 16 }}>
            <Button style={{ borderColor: "#2962FF", color: "#2962FF" }}>
              200
            </Button>
          </div>
          <div style={{ marginTop: 8 }}>
            <div className={styles.tree}>
              <Tree
                treeData={newTreeData}
                selectable
                onSelect={(keys: Key[]) => {
                  const mainKey: any = get(keys, "[0]");
                  if (activeResponseName && typeof mainKey === "string") {
                    const source = mainKey?.replace("_", ".");

                    const cloneObj = clone(responseMapping);
                    const index = cloneObj.findIndex(
                      (i: any) => i.name === activeResponseName
                    );
                    set(cloneObj, `[${index}].source`, `{{${source}}}`);
                    set(cloneObj, `[${index}].sourceLocation`, `BODY`);
                    setResponseMapping(cloneObj);
                    setActiveResponseName(undefined);
                  }
                }}
              />
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default SelectResponseProperty;
