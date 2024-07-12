import Flex from "@/components/Flex";
import { get, isArray, isBoolean, isEmpty, isObject } from "lodash";
import { TreeDataNode, Typography } from "antd";

const { Text } = Typography;

const buildPrefix = (prefix: string, key: string, arrayPrefix?: string) => {
  if (prefix) {
    return `${prefix}${arrayPrefix || ""}.${key}`;
  }
  return `${key}${arrayPrefix || ""}`;
};

export const findSchema = (firstURL: string, schemas: any) => {
  let schema = get(schemas, `${firstURL}.properties`);
  let schemaUrl = firstURL;
  if (isEmpty(schema)) {
    schemaUrl = get(schemas, `${firstURL}.allOf[0].$ref`, "").replace(
      "#/components/schemas/",
      ""
    );
    schema = get(schemas, `${schemaUrl}.properties`);
    if (isEmpty(schema)) {
      return null;
    }
  }
  return schemaUrl;
};

export const schemaParses = (
  schemaUrl: string,
  schemas: any,
  prefix = "",
  nodeTitleClassName: string,
  nodeExampleClassName: string
) => {
  const newSchemaURL = findSchema(schemaUrl, schemas);
  if (isEmpty(newSchemaURL) || typeof newSchemaURL !== "string") {
    return undefined;
  }
  const result: any = Object.entries(
    get(schemas, `${newSchemaURL}.properties`)
  ).map(([name, { type, example, $ref }]: any) => {
    const url = $ref?.replace("#/components/schemas/", "");
    return {
      title: (
        <Flex
          justifyContent="flex-start"
          style={{ width: "100%", alignItems: "stretch" }}
          gap={3}
        >
          <span className={nodeTitleClassName}>"{name}"</span>
          {!$ref ? (
            <span className={nodeExampleClassName}>
              {example ? `"${example}"` : type}
            </span>
          ) : (
            <span className={nodeExampleClassName}>"{url}"</span>
          )}
        </Flex>
      ),
      key: prefix + name + type,
      type,
      example,
      children: schemaParses(
        url,
        schemas,
        `${name + type}`,
        nodeTitleClassName,
        nodeExampleClassName
      ),
    };
  });
  return result;
};

const renderValue = (value: any, typeOfValue: string) => {
  if (typeOfValue === "string") {
    return <>"{value}"</>;
  }
  if (["number", "boolean"].includes(typeOfValue)) {
    return <>{`${value}`}</>;
  }
  if (isArray(value)) {
    return <>"array"</>;
  }
  if (isObject(value)) {
    return <>"object"</>;
  }
  return <>&nbsp;</>;
};

export const parseObjectDescriptionToTreeData = (
  keys: Record<string, any>,
  titleClassName: string,
  exampleClassName: string,
  level = 0,
  prefix = ""
) => {
  if (!keys) return [];
  const result: any = Object.entries(keys).map(([key, value]) => {
    const typeOfValue = typeof value;

    let children = undefined;
    if (isObject(value)) {
      children = parseObjectDescriptionToTreeData(
        value as Record<string, any>,
        titleClassName,
        exampleClassName,
        level + 1,
        buildPrefix(prefix, key)
      );
    }
    if (isArray(value)) {
      children = parseObjectDescriptionToTreeData(
        get(value, "[0]", {}) as Record<string, any>,
        titleClassName,
        exampleClassName,
        level + 1,
        buildPrefix(prefix, key, "[*]")
      );
    }
    return {
      title: (
        <Flex justifyContent="flex-start" style={{ width: "100%" }} gap={4}>
          <Text className={titleClassName} ellipsis={{ tooltip: true }}>
            "{key}"
          </Text>
          <Text
            className={exampleClassName}
            style={{ flex: `0 0 calc((40% + ${24 * level * 0.4}px))` }}
            ellipsis={{ tooltip: true }}
          >
            {renderValue(value, typeOfValue)}
          </Text>
        </Flex>
      ),
      key: buildPrefix(prefix, key, isArray(value) ? "[*]" : ""),
      selectable: typeOfValue !== "object",
      children,
    };
  });
  return result;
};

export const convertSchemaToTypeOnly = (keys: Record<string, any>) => {
  const result: any = {};
  if (isEmpty(keys)) {
    return {};
  }
  Object.entries(keys).forEach(([key, propData]) => {
    if (["string", "number", "boolean"].includes(propData.type)) {
      result[key] = propData.type;
    }
    if (propData.type === "array") {
      result[key] = [convertSchemaToTypeOnly(propData.items.properties)];
    }
    if (propData.type === "object") {
      result[key] = convertSchemaToTypeOnly(propData.properties);
    }
  });
  return result;
};

export const renderExampleValue = (value: any) => {
  if (typeof value === "string") {
    return `"${value}"`;
  }
  if (isArray(value)) {
    return `"array"`;
  }
  if (isObject(value)) {
    return `"object"`;
  }
  if (isBoolean(value)) {
    return "boolean";
  }
  return "";
};

/**
 * Parses the given example object into a tree data structure.
 * @param example - The example object to parse.
 * @param prefix - The prefix to use for the keys in the tree data. Default is an empty string.
 * @param nodeTitleClassName - The CSS class name for the title element.
 * @param nodeExampleClassName - The CSS class name for the example element.
 * @returns The parsed tree data structure, or undefined if the example is empty.
 */
export const exampleParse = (
  example: Record<string, any>,
  prefix = "" as string,
  nodeTitleClassName = "" as string,
  nodeExampleClassName = "" as string
): TreeDataNode[] | undefined => {
  if (isEmpty(example)) {
    return undefined;
  }
  const keys = Object.keys(example);
  return keys?.map((key: string) => {
    const nodeTitle = (
      <Flex
        justifyContent="flex-start"
        style={{ width: "100%", alignItems: "stretch" }}
        gap={3}
      >
        <span className={nodeTitleClassName}>{key}</span>

        <span className={nodeExampleClassName}>
          {renderExampleValue(example[key])}
        </span>
      </Flex>
    );
    const isValueArray = isArray(example[key])
      ? `[${example[key].length > 1 ? "*" : "0"}]`
      : "";
    const children =
      !isEmpty(example[key]) && typeof example[key] === "object"
        ? exampleParse(
            isArray(example[key]) ? example[key][0] : example[key],
            buildPrefix(prefix, key, isValueArray),
            nodeTitleClassName,
            nodeExampleClassName
          )
        : undefined;
    return {
      key: buildPrefix(prefix, key, isValueArray),
      title: nodeTitle,
      children,
    };
  });
};
