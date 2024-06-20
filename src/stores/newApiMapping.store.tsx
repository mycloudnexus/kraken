import { EnumRightType } from "@/utils/types/common.type";
import { create } from "zustand";

type NewApiMappingStore = {
  query?: string;
  sellerApi: any;
  rightSide?: EnumRightType;
  requestMapping: any[];
  responseMapping: any;
  setQuery: (q: string) => void;
  setSellerApi: (api: any) => void;
  setRightSide: (side?: EnumRightType) => void;
  setRequestMapping: (mapping: any[]) => void;
  setResponseMapping: (mapping: any) => void;
  reset: () => void;
  activeResponseName?: string;
  setActiveResponseName: (a?: string) => void;
};

const defaultData = {
  query: undefined,
  sellerApi: undefined,
  rightSide: undefined,
  requestMapping: [],
  responseMapping: undefined,
  activeResponseName: undefined,
};

export const useNewApiMappingStore = create<NewApiMappingStore>()((set) => ({
  ...defaultData,
  setQuery: (query: string) => set({ query }),
  setSellerApi: (sellerApi: any) => set({ sellerApi }),
  setRightSide: (rightSide) => set({ rightSide }),
  setRequestMapping: (requestMapping) => set({ requestMapping }),
  setResponseMapping: (responseMapping) => set({ responseMapping }),
  reset: () => set(defaultData),
  setActiveResponseName: (value?: string) => set({ activeResponseName: value }),
}));
