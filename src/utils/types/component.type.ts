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

export interface IRequestMapping {
  name: string;
  description: string;
  source: string;
  sourceLocation: string;
  target: string;
  targetLocation: string;
  title: string;
  requiredMapping?: boolean;
}
