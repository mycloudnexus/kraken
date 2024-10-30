import { create } from "zustand";

export interface IReleaseParams {
  hasUpgrade?: boolean;
  orderBy?: string;
  direction?: string;
  page?: number;
  size?: number;
}

type MappingTemplateStore = {
  releaseParams: IReleaseParams;
  setReleaseParams: (releaseParams: Partial<IReleaseParams>) => void;
  upgradeParams: IReleaseParams;
  setUpgradeParams: (upgradeParams: Partial<IReleaseParams>) => void;
};

export const defaultData = {
  releaseParams: {
    hasUpgrade: false,
    orderBy: "createdAt",
    direction: "DESC",
    page: 0,
    size: 100,
  },
  upgradeParams: {
    hasUpgrade: false,
    orderBy: "createdAt",
    direction: "DESC",
    page: 0,
    size: 20,
  },
};

export const useMappingTemplateStore = create<MappingTemplateStore>((set) => ({
  ...defaultData,
  setReleaseParams: (data) =>
    set((state) => ({ releaseParams: { ...state.releaseParams, ...data } })),
  setUpgradeParams: (data) =>
    set((state) => ({ upgradeParams: { ...state.upgradeParams, ...data } })),
}));
