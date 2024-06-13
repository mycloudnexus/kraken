export interface IComponentVersion {
  id: string;
  createdAt: string;
  name: string;
  version: string;
  key: string;
}

export interface IProductWithComponentVersion {
  key: string;
  name: string;
  componentVersions: IComponentVersion[];
}
