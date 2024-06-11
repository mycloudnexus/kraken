import Flex from "@/components/Flex";
import { get, isEmpty } from "lodash";

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
