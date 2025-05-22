import { IMappers } from "@/utils/types/component.type";
import { chain, flatMap } from "lodash";
import { IMapping } from "./components/ResponseMapping";
import { nanoid } from "nanoid";

/**
 *  Returns properties id whose source/target/location data is missing (applicable for seller apis, and sonata's custom fields only)
 *
 */
export function validateMappers(mappers: IMappers) {
  const requestIds = mappers.request
    .filter((property) => {
      if (!property.customizedField)
        return property.target && !property.targetLocation;

      return (
        !property.source ||
        !property.sourceLocation ||
        !property.target ||
        !property.targetLocation
      );
    })
    .map((property) => property.id!);

  const responseIds = mappers.response
    .filter((property) => {
      if (!property.customizedField)
        return property.source && !property.sourceLocation;

      return (
        !property.source ||
        !property.sourceLocation ||
        !property.target ||
        !property.targetLocation
      );
    })
    .map((property) => property.id!);

  let errorMessage: string | undefined = undefined;
  if (requestIds.length && responseIds.length) {
    errorMessage =
      "Some customized properties from Request and Response mapping are empty, please check.";
  } else if (requestIds.length) {
    errorMessage =
      "Some customized properties from Request mapping are empty, please check.";
  } else if (responseIds.length) {
    errorMessage =
      "Some customized properties from Response mapping are empty, please check.";
  }

  return {
    requestIds: new Set(requestIds),
    responseIds: new Set(responseIds),
    errorMessage,
  };
}

export function locationMapping(
  loc: string,
  type: "request" | "response"
): string {
  switch (loc) {
    case "BODY":
      return type === "request" ? "Request body" : "Response body";
    case "QUERY":
      return "Query parameter";
    case "PATH":
      return "Path parameter";
    case "HYBRID":
      return "Hybrid";
    case "CONSTANT":
      return "Constant";
    default:
      return loc;
  }
}

export function renderDeployText(status: string): string {
  switch (status) {
    case "SUCCESS":
      return "success.";
    case "IN_PROCESS":
      return "in process.";
    case "FAILED":
      return "failed.";
    default:
      return "";
  }
}

export function transformListMappingItem(
  item: IMapping[],
  type: "request" | "response"
) {
  return chain(item)
    .groupBy("name")
    .map((items, name) => ({
      name,
      valueMapping: flatMap(items, (item) =>
        // item?.to?.map((to) => ({ [to]: item.from }))
        type === "request"
          ? [{ [item.from as string]: item.to?.[0] }]
          : item?.to?.map((to) => ({ [to]: item.from }))
      ),
    }))
    .value();
};

export function handleDeleteMappingItems(
  key: React.Key,
  listMapping: IMapping[],
  emptyToValue: any
): IMapping[] | undefined {
  const targetItem = listMapping.find((item) => item.key === key);
  if (!targetItem) {
    return;
  }

  const filtered = listMapping.filter((item) => item.key !== key);
  const remainingGroupItems = filtered.filter(item => item.name === targetItem.name);
  const updated = [...filtered];

  if (remainingGroupItems.length === 0) {
    updated.push({
      name: targetItem.name,
      key: nanoid(),
      from: undefined,
      to: emptyToValue,
    });
  }

  return updated;
}