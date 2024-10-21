export interface IPagingData<T> {
  data: T[];
  total: number;
  page: number;
  size: number;
}

export interface IDetailsData<T> {
  details: T[];
}
export interface IPagingParams {
  page: number;
  size: number;
}

export interface IMetadata {
  id: string;
  name: string;
  version: number;
  key: string;
  description: string;
  productKey: string;
  tags: string[];
  labels: Record<string, any>;
  referApiSpec: string;
  referWorkflow: string;
  logo: string;
}

export interface IAssetLink {
  targetAssetKey: string;
  relationship: string;
}

export interface ISyncMetadata {
  fullPath: string;
  syncedSha: string;
  syncedAt: string;
}

export interface IUnifiedAsset {
  kind: string;
  inUse: boolean;
  apiVersion: string;
  metadata: IMetadata;
  facets: Record<string, any>;
  links: IAssetLink[];
  id: string;
  parentId: string;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
  syncMetadata: ISyncMetadata;
}

interface ControlPlane {
  kind: string;
  apiVersion: string;
  metadata: IMetadata;
  facets: Record<string, any>;
  links: any[];
  id: string;
  parentId: string;
  createdAt: string;
  updatedAt: string;
  syncMetadata: ISyncMetadata;
}

export interface IEndpointUsageAsset {
  endpointUsage: {
    controlPlane: ControlPlane[];
    dataPlaneProduction: any[];
    dataPlaneStage: any[];
  };
}

export enum EnumRightType {
  SelectSellerAPI,
  AddSonataProp,
  AddSellerProp,
  AddSellerResponse,
  SonataResponse,
}

export type ActiveTabType = "request" | "response";
