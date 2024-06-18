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
