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
  customizedField?: boolean;
  sourceValues?: string[];
  valueMapping?: Record<string, any>;
  allowValueLimit?: boolean;
  discrete?: boolean;
  sourceType?: string;
  // Custom field
  id: string;
}

export interface IBuyer extends IComponent {
  buyerToken: {
    accessToken: string;
    expiredAt: string;
  };
  createdBy?: string;
}
export interface IResponseMapping {
  name: string;
  title: string;
  source: string;
  target: string;
  targetType: string;
  description: string;
  sourceLocation: string;
  targetLocation: string;
  requiredMapping: boolean;
  targetValues: string[];
  customizedField?: boolean;
  valueMapping?: Record<string, any>;
  // Custom field
  id?: string;
}

export interface IComponent {
  kind: string;
  apiVersion: string;
  metadata: IMetadata;
  facets: IFacets & Record<string, any>;
  links: any[];
  id: string;
  inUse?: boolean;
  parentId: string;
  createdAt: string;
  updatedAt: string;
  updatedBy: string;
  syncMetadata: ISyncMetadata;
}
export interface ISyncMetadata {
  fullPath: string;
  syncedSha: string;
  syncedAt: string;
  syncedBy: string;
}
export interface IFacets {
  endpoints: Endpoint[];
  trigger: ITrigger;
}
export interface ITrigger {
  path: string;
  method: string;
  addressType: string;
  provideAlternative: boolean;
}
export interface Endpoint {
  id: string;
  path: string;
  method: string;
  mappers: IMappers;
  serverKey: string;
}
export interface IMappers {
  request: IRequestMapping[];
  response: IResponseMapping[];
}
export interface IMetadata {
  id: string;
  name: string;
  version: number;
  key: string;
  description: string;
  labels: ILabels;
  status?: string;
}
export interface ILabels {
  deployedStatus: string;
  stageDeployedStatus: string;
  subVersion: string;
  version: string;
}
