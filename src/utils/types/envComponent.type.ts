export interface IComponentVersion {
  id: string;
  productId: string;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
  name: string;
  version: string;
  key: string;
  componentName: string;
}

export interface IEnvComponent {
  id: string;
  productId: string;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
  name: string;
  status: string;
  components: IComponentVersion[];
}
