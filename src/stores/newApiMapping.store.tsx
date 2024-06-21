import { EnumRightType } from "@/utils/types/common.type";
import { omit } from "lodash";
import { create } from "zustand";

type NewApiMappingStore = {
  query?: string;
  sellerApi: any;
  rightSide?: EnumRightType;
  requestMapping: any[];
  responseMapping: any[];
  rightSideInfo: any;
  setQuery: (q: string) => void;
  setSellerApi: (api: any) => void;
  setRightSide: (side?: EnumRightType) => void;
  setRequestMapping: (mapping: any[]) => void;
  setResponseMapping: (mapping: any) => void;
  setRightSideInfo: (rightSideInfo: any) => void;
  reset: () => void;
  activeResponseName?: string;
  setActiveResponseName: (a?: string) => void;
};

const defaultData = {
  query: undefined,
  sellerApi: undefined,
  rightSide: undefined,
  requestMapping: [],
  responseMapping: [],
  rightSideInfo: undefined,
  activeResponseName: undefined,
};

export const useNewApiMappingStore = create<NewApiMappingStore>()((set) => ({
  ...defaultData,
  setQuery: (query) => set({ query }),
  setSellerApi: (sellerApi) => set({ sellerApi }),
  setRightSide: (rightSide) => set({ rightSide }),
  setRequestMapping: (requestMapping) => set({ requestMapping }),
  setResponseMapping: (responseMapping) => set({ responseMapping }),
  setRightSideInfo: (rightSideInfo) => set({ rightSideInfo }),
  reset: () => set(omit(defaultData, "query")),
  setActiveResponseName: (value?: string) => set({ activeResponseName: value }),
}));
