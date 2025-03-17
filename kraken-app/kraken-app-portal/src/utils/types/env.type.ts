export interface IEnvComponent {
  id: string;
  name: string;
  version: string;
}

export interface IEnv {
  id: string;
  name: string;
  productId: string;
  createdAt: string;
  components?: IEnvComponent[];
}

export interface IActivityLog {
  env: string;
  requestId: string;
  uri: string;
  path: string;
  method: string;
  buyerName: string;
  queryParameters: Record<string, any>;
  headers: Record<string, any>;
  request: Record<string, any>;
  response: Record<string, any>;
  createdAt: string;
  updatedAt: string;
  httpStatusCode: number;
  requestIp: string;
  responseIp: string;
  callSeq: number;
}

export interface IPushHistory {
  id: string;
  createdAt: string;
  envName: string;
  startTime: string;
  endTime: string;
  pushedBy: string;
  status: string
};

export interface IActivityDetail {
  main: IActivityLog;
  branches: IActivityLog[];
}

export interface IApiKeyDetail {
  id: string;
  productId: string;
  createdAt: string;
  name: string;
  envId: string;
  expiresAt: number;
  revoked: boolean;
}
export interface IDataPlaneDetail {
  id: string;
  updatedAt: string;
  createdAt: string;
  clientIp: string;
  envId: string;
  status: string;
}

export interface IRunningComponent {
  id: string;
  name: string;
  version: string;
  key: string;
  componentName: string;
}

type MappingMatrix = Record<string, string | boolean>;

export interface IRunningMapping {
  targetMapperKey: string;
  path: string;
  method: string;
  requiredMapping: boolean;
  diffWithStage: boolean;
  productType?: string;
  actionType?: string;
  mappingMatrix: MappingMatrix;
  componentName: string;
  componentKey: string;
  createAt: string;
  createBy?: string;
  userName?: string;
  version: string;
  status: string;
  mappingStatus: string;
}

export interface IRunningComponentItem {
  id: string;
  updatedAt?: string;
  createdAt?: string;
  name: string;
  status: string;
  components: IRunningComponent[];
}
export interface ICreateParameter {
  productId: string;
  envId: string;
  name: string;
}
export interface ICreateParameter {
  productId: string;
  envId: string;
  name: string;
}

export interface IMapperDetails {
  targetKey: string;
  targetMapperKey: string;
  description: string;
  path: string;
  method: string;
  productType?: string;
  actionType?: string;
  mappingStatus: string;
  mappingMatrix: MappingMatrix;
  updatedAt: string;
  lastDeployedAt?: string;
  diffWithStage?: boolean;
  requiredMapping: boolean;
  orderBy: string;
  order?: number;
}

export interface ILogActivity {
  id: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  userId: string;
  email: string;
  name: string;
  path: string;
  method: string;
  pathVariables: Record<string, any>;
  action: string;
  description: string;
  resource: string;
  resourceId: string;
  remoteAddress: string;
  statusCode: number;
  ignoreRequestParams: any[];
  request: Record<string, any>;
  response: Record<string, any>;
}

export type DataPlaneUpgradeCheck = {
  errorMessages: string[]
  mapperCompleted: boolean
  newerTemplate: boolean
  compatible: boolean
}
