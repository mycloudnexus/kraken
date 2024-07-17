import { IMapperDetails } from '../types/env.type';

export interface GroupedByPath {
  [path: string | number]: IMapperDetails[];
}

export default function groupByPath(data: IMapperDetails[]): GroupedByPath {
  return data.reduce<GroupedByPath>((acc, item) => {
      const { path } = item;
      if (!acc[path]) {
          acc[path] = [];
      }
      acc[path].push(item);
      return acc;
  }, {});
}