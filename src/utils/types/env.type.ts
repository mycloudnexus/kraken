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
