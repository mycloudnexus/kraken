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

export interface IActivityDetail {
  main: IActivityLog;
  branches: IActivityLog[];
}

export interface IApiKeyDetail {
  id: string,
  productId: string,
  createdAt: string,
  name: string,
  envId: string,
  expiresAt: number,
  revoked: boolean
}
export interface IDataPlaneDetail {
  id: string,
  updatedAt: string,
  createdAt: string,
  clientIp: string,
  envId: string,
  status: string,

}

export interface IRunningComponent {
  id: string,
  name: string,
  version: string,
  key: string,
  componentName: string,


}
export interface IRunningComponentItem {
  id: string,
  updatedAt: string,
  createdAt: string,
  name: string,
  status: string,
  components: IRunningComponent[],

}


