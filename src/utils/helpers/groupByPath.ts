import { omit } from "lodash";
import { IMapperDetails } from "../types/env.type";

export interface GroupedByPath {
  [path: string | number]: IMapperDetails[];
}

export default function groupByPath(data: IMapperDetails[]): GroupedByPath {
  const groupedData: GroupedByPath = {};
  const rootToPathMap: { [root: number]: string } = {};

  data.forEach((item) => {

    // Extract the root position and order from the 'orderBy' field
    const [root, order] = item.orderBy
      .replace(/[<>]/g, "")
      .split(",")
      .map(Number);

    // Map the root position to the path
    if (!rootToPathMap[root]) {
      rootToPathMap[root] = item.path;
    }
    const path = rootToPathMap[root];

    // Create the group if it doesn't exist
    if (!groupedData[path]) {
      groupedData[path] = [];
    }

    // Add the item to the appropriate group with order
    groupedData[path].push({ ...item, order });
  });

  // Sort each group by the inner order value
  Object.keys(groupedData).forEach((path) => {
    groupedData[path].sort((a, b) => (a as any).order - (b as any).order);
  });

  // Clean up the sorted groups by removing the order key
  Object.keys(groupedData).forEach((path) => {
    groupedData[path] = groupedData[path].map((item: IMapperDetails) =>
      omit(item, ["order"])
    );
  });
  return groupedData;
}
