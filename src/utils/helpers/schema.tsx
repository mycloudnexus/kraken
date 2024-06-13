import Flex from "@/components/Flex";
import { get, isArray, isBoolean, isEmpty, isObject } from "lodash";
import { TreeDataNode, Typography } from "antd";

const { Text } = Typography;

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
  return <>&nbsp;</>;
};

export const parseObjectDescriptionToTreeData = (
  keys: Record<string, any>,
  titleClassName: string,
  exampleClassName: string,
  level = 0,
  prefix = ""
) => {
  const result: any = Object.entries(keys).map(([key, value]) => {
    const typeOfValue = typeof value;

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
      key: `${prefix}_${key}`,
      children:
        typeOfValue === "object"
          ? parseObjectDescriptionToTreeData(
              value as Record<string, any>,
              titleClassName,
              exampleClassName,
              level + 1,
              `${prefix}_${key}`
            )
          : undefined,
    };
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
    return `"array"`;
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
  return keys.map((key: string) => {
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

    const children =
      !isEmpty(example[key]) && typeof example[key] === "object"
        ? exampleParse(
            isArray(example[key]) ? example[key][0] : example[key],
            `${prefix}_${key}`,
            nodeTitleClassName,
            nodeExampleClassName
          )
        : undefined;

    return {
      key: `${prefix}_${key}`,
      title: nodeTitle,
      children,
    };
  });
};
