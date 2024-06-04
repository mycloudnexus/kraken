export interface IComponent {
  kind: string;
  apiVersion: string;
  metadata: IMetadata;
  id: string;
  organizationId: string;
  parentId: string;
  createdAt: string;
  updatedAt: string;
}

export interface IMetadata {
  id: string;
  name: string;
  version: number;
  key: string;
  description?: string;
}
