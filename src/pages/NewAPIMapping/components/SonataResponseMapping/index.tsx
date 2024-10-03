import { Button, Flex, Input, Tree, notification } from "antd";
import styles from "./index.module.scss";
import Text from "@/components/Text";
import { Key, useCallback, useEffect, useMemo, useState } from "react";
import { getCorrectSpec } from "../RightAddSonataProp";
import swaggerClient from "swagger-client";
import { clone, get, isEmpty, set } from "lodash";
import {
  convertSchemaToTypeOnly,
  exampleParse,
  parseObjectDescriptionToTreeData,
} from "@/utils/helpers/schema";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { useBoolean } from "usehooks-ts";
import { DownOutlined, RightOutlined } from "@ant-design/icons";
import TitleIcon from "@/assets/title-icon.svg";

interface Props {
  spec: any;
  method: string;
}

const Search = Input.Search;

const SonataResponseMapping = ({ method, spec }: Props) => {
  const [searchValue, setSearchValue] = useState("");
  const [resolvedSpec, setResolvedSpec] = useState<any>();
  const [selectedKeys, setSelectedKeys] = useState<Key[]>([]);
  const {
    setResponseMapping,
    activeSonataResponse,
    responseMapping,
    setActiveSonataResponse,
  } = useNewApiMappingStore();

  const { value: isOpen, toggle: toggleOpen } = useBoolean(true);

  const transformString = (currentInput: string) => {
    if (currentInput.indexOf("[*]") === -1) {
      return currentInput;
    }
    const parts = currentInput
      ?.replaceAll("0[*]", "[*]")
      ?.replaceAll("[*]", "[0]")
      .split(".");
    const newString = parts.join(".")?.replace("[0]", "[*]");
    return newString;
  };

  const handleOK = () => {
    const key = get(selectedKeys, "[0]");
    const newKey = transformString(key as unknown as string);
    if (activeSonataResponse && typeof key === "string") {
      const [index, name] = activeSonataResponse.split("-");
      if (
        !index ||
        get(responseMapping, `[${[index]}].name`, "undefined") !== name
      ) {
        notification.error({ message: "Error. Please try again" });
        return;
      }
      const cloneObj = clone(responseMapping);

      const value = `@{{${newKey}}}`;
      set(cloneObj, `[${index}].target`, value);
      set(cloneObj, `[${index}].targetLocation`, `BODY`);
      setResponseMapping(cloneObj);
      setActiveSonataResponse(undefined);
      setSelectedKeys([]);
    }
  };

  const handleSelect = (keys: Key[]) => {
    if (!activeSonataResponse) {
      notification.warning({
        message: "Please select property from Sonata API response first",
      });
      return;
    }
    const mainKey: any = get(keys, "[0]");
    setSelectedKeys([mainKey]);
  };

  useEffect(() => {
    if (!spec) return;
    (async () => {
      const result = await swaggerClient.resolve({ spec });
      setResolvedSpec(result.spec);
    })();
  }, [spec]);

  const correctSpec = useMemo(
    () => getCorrectSpec(resolvedSpec, method),
    [resolvedSpec, method]
  );

  const responseData = useMemo(() => {
    if (correctSpec) {
      const responses = get(
        correctSpec,
        "responses.200.content",
        get(correctSpec, "responses.201.content")
      );
      if (isEmpty(responses)) {
        return null;
      }
      const objectKey = get(Object.keys(responses), "[0]", "");
      const example: any = get(
        responses,
        `[${objectKey}].examples.response.value`,
        get(
          responses,
          `content[${objectKey}].example`,
          get(responses, `[${objectKey}].examples`)
        )
      );
      if (!isEmpty(example)) {
        return exampleParse(example, "", styles.nodeTitle, styles.nodeExample);
      }
      const properties = get(
        responses,
        `[${objectKey}].schema.items.properties`,
        get(responses, `[${objectKey}].schema.properties`, {})
      );
      if (!isEmpty(properties)) {
        const simplifiedProperties = convertSchemaToTypeOnly(properties);
        return parseObjectDescriptionToTreeData(
          simplifiedProperties,
          styles.nodeTitle,
          styles.nodeExample,
          undefined,
          undefined,
          true
        );
      }
      return [];
    }
  }, [correctSpec]);

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

  const treeData = useMemo(
    () => findMatchingElements(responseData, searchValue),
    [responseData, searchValue]
  );

  return (
    <Flex vertical gap={16} style={{ width: "100%", height: "100%" }}>
      <div className={styles.header}>
        <Text.Custom size="15px" bold="500">
          Select Sonata API mapping property
        </Text.Custom>
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
            style={{ cursor: "pointer" }}
            justify="space-between"
            gap={8}
            align="center"
            onClick={toggleOpen}
            role="none"
          >
            <Flex justify="flex-start" gap={8} align="center">
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
            <div style={{ marginTop: 12, marginBottom: 4 }}>
              <Button style={{ borderColor: "#2962FF", color: "#2962FF" }}>
                200
              </Button>
            </div>
            <div className={styles.tree}>
              <Tree
                selectedKeys={selectedKeys}
                treeData={treeData}
                onSelect={handleSelect}
                selectable
              />
            </div>
          </>
        )}
      </div>
      <Flex justify="flex-end" className={styles.footer}>
        <Button
          data-testid="seller-prop-ok"
          type="primary"
          disabled={isEmpty(selectedKeys)}
          onClick={handleOK}
        >
          OK
        </Button>
      </Flex>
    </Flex>
  );
};

export default SonataResponseMapping;
