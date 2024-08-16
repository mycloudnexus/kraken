import { create } from "zustand";

interface IParams {
  envId?: string;
  page: number;
  size: number;
}

type DeploymentStore = {
  params: IParams;
  setParams: (params: Partial<IParams>) => void;
  resetParams: () => void;
};

const DEFAULT_PARAMS = {
  page: 0,
  size: 10,
  orderBy: "createdAt",
  direction: "DESC",
};

export const useDeploymentStore = create<DeploymentStore>()((set) => ({
  params: DEFAULT_PARAMS,
  setParams: (data) =>
    set((state) => ({ params: { ...state.params, ...data } })),
  resetParams: () => set(() => ({ params: DEFAULT_PARAMS })),
}));
