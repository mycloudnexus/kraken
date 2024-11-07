import { IRunningMapping } from "./env.type";

export interface IMetadata {
  id: string;
  name: string;
  version: number;
  key: string;
  description?: string;
  status?: string;
}

export interface INewVersionParams {
  productId: string;
  componentId: string;
  componentKey: string;
  versionName?: string;
  version?: string;
}

export interface IDeploymentHistory {
  targetMapperKey: string;
  path: string;
  method: string;
  requiredMapping: boolean;
  diffWithStage: boolean;
  mappingMatrix: IMappingMatrix;
  componentName: string;
  componentKey: string;
  envId: string;
  envName: string;
  createAt: string;
  createBy: string;
  userName: string;
  releaseKey: string;
  releaseId: string;
  tagId: string;
  version: string;
  status: string;
  verifiedBy: string;
  verifiedAt: string;
  verifiedStatus: boolean;
}

export interface IMappingMatrix {
  provideAlternative: boolean;
  addressType: string;
  productType?: string;
  actionType?: string;
}

export interface IReleaseHistory {
  templateUpgradeId: string;
  name: string;
  productVersion: string;
  publishDate: string;
  description: string;
  deployments: Deployment[];
  showStageUpgradeButton: boolean;
  showProductionUpgradeButton: boolean;
  productSpec: string;
  status: string;
}

export interface Deployment {
  deploymentId: string;
  templateUpgradeId: string;
  envName: string;
  productVersion: string;
  upgradeBy: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface IUpgrade {
  deploymentId: string;
  templateUpgradeId: string;
  envName: string;
  productVersion: string;
  upgradeBy: string;
  status: string;
  createdAt: string;
}

export interface IApiMapperDeployment {
  envId: string;
  envName: string;
  mapperKey: string;
  runningVersion: string;
  status: string;
  createAt: string;
  createBy: string;
}

export interface IQuickStartGuideObject {
  sellerApiServerRegistrationInfo: {
    atLeastOneSellerApiRegistered: boolean;
  };
  apiMappingInfo: {
    atLeastOneMappingCompleted: boolean;
  };
  deploymentInfo: {
    atLeastOneApiDeployedToStage: boolean;
    atLeastOneBuyerRegistered: boolean;
    atLeastOneApiDeployedToProduction: boolean;
  };
}

type RequestStatistics = {
  date: string;
  success: number;
  error: number;
};
export interface IApiActivity {
  requestStatistics: Array<RequestStatistics>;
}

export type ErrorBrakedown = {
  date: string;
  errors: {
    400: number;
    401: number;
    404: number;
    500: number;
  };
};
export interface IErrorBrakedown {
  errorBreakdowns: Array<ErrorBrakedown>;
}

type MostPopularEndpoints = {
  method: string;
  endpoint: string;
  usage: number;
  popularity: number;
};
export interface IMostPopularEndpoints {
  endpointUsages: Array<MostPopularEndpoints>;
}

export interface IApiUseCase {
  componentKey: string;
  componentName: string;
  details: IRunningMapping[];
}

export interface IProductIdAndNameParams {
  productId: string;
  name: string;
}