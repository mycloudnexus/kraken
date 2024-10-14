import { IComponent } from "./product.type";

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
}

export interface IBuyer extends IComponent {
  buyerToken: {
    accessToken: string;
    expiredAt: string;
  };
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
}
