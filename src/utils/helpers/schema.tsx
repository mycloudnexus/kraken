import Flex from "@/components/Flex";
import { get, isEmpty } from "lodash";
import { Typography } from "antd";

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
