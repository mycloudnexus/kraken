export interface IUser {
  id: string;
  productId: string;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
  name: string;
  email: string;
  role: string;
  state: string;
}

export interface ISystemInfo {
  id: string
  createdAt: string
  updatedAt: string
  controlProductVersion: string
  stageProductVersion: string
  productionProductVersion: string
  controlAppVersion: string
  productKey: string
  productName: string
  productSpec: string
  key: string
  status: string
  productionAppVersion: string
  stageAppVersion: string
}
