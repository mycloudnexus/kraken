export interface IComponent {
  kind: string;
  apiVersion: string;
  metadata: IMetadata;
  id: string;
  organizationId: string;
  parentId: string;
  createdAt: string;
  updatedAt: string;
  facets: Record<string, any>;
}

export interface IMetadata {
  id: string;
  name: string;
  version: number;
  key: string;
  description?: string;
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
}

export interface IReleaseHistory {
  templateUpgradeId: string;
  name: string;
  productVersion: string;
  publishDate: string;
  description: string;
  deployments: Deployment[];
  showUpgradeButton: boolean;
  productSpec?: string;
}

export interface Deployment {
  deploymentId: string;
  templateUpgradeId: string;
  envName: string;
  releaseVersion: string;
  upgradeBy: string;
  status: string;
  createdAt: string;
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
