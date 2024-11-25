import { create } from "zustand";

interface IParams {
  envId?: string;
  page: number;
  size: number;
  search?: string;
}

type BuyerStore = {
  params: IParams;
  setParams: (params: Partial<IParams>) => void;
  resetParams: () => void;
};

const DEFAULT_PARAMS = {
  page: 0,
  size: 20,
  orderBy: "createdAt",
  direction: "DESC",
};

export const useBuyerStore = create<BuyerStore>()((set) => ({
  params: DEFAULT_PARAMS,
  setParams: (data) =>
    set((state) => ({ params: { ...state.params, ...data } })),
  resetParams: () => set(() => ({ params: DEFAULT_PARAMS })),
}));
