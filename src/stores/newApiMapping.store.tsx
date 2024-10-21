import { IMapping } from "@/pages/NewAPIMapping/components/ResponseMapping";
import { EnumRightType } from "@/utils/types/common.type";
import { omit } from "lodash";
import { create } from "zustand";

type NewApiMappingStore = {
  query?: string;
  sellerApi: any;
  rightSide?: EnumRightType;
  serverKey?: string;
  requestMapping: any[];
  responseMapping: any[];
  rightSideInfo: any;
  setQuery: (q: string) => void;
  setSellerApi: (api: any) => void;
  setRightSide: (side?: EnumRightType) => void;
  setServerKey: (key: string) => void;
  setRequestMapping: (mapping: any[]) => void;
  setResponseMapping: (mapping: any) => void;
  setRightSideInfo: (rightSideInfo: any) => void;
  reset: () => void;
  activeResponseName?: string;
  setActiveResponseName: (a?: string) => void;
  listMappingStateResponse: IMapping[];
  setListMappingStateResponse: (value?: IMapping[]) => void;
  sellerAPIExampleProps: {
    path: Record<string, string>;
    param: Record<string, string>;
  };
  setSellerAPIExampleProps: (value: any) => void;
  activeSonataResponse?: string;
  setActiveSonataResponse: (value?: string) => void;
  listMappingStateRequest: IMapping[];
  setListMappingStateRequest: (value?: IMapping[]) => void;
  errors?: {
    requestIds: Set<string>;
    responseIds: Set<string>;
  };
  setErrors(value: { requestIds: Set<string>; responseIds: Set<string> }): void;
};

const defaultData = {
  query: undefined,
  sellerApi: undefined,
  rightSide: EnumRightType.SelectSellerAPI,
  serverKey: undefined,
  requestMapping: [],
  responseMapping: [],
  rightSideInfo: undefined,
  activeResponseName: undefined,
  listMappingStateResponse: [],
  sellerAPIExampleProps: {
    path: {},
    param: {},
  },
  activeSonataResponse: undefined,
  listMappingStateRequest: [],
};

export const useNewApiMappingStore = create<NewApiMappingStore>()((set) => ({
  ...defaultData,
  setQuery: (query) => set({ query }),
  setSellerApi: (sellerApi) => set({ sellerApi }),
  setRightSide: (rightSide) => set({ rightSide }),
  setServerKey: (serverKey) => set({ serverKey }),
  setRequestMapping: (requestMapping) => set({ requestMapping }),
  setResponseMapping: (responseMapping) => set({ responseMapping }),
  setRightSideInfo: (rightSideInfo) => set({ rightSideInfo }),
  reset: () => set(omit(defaultData, ["query"])),
  setActiveResponseName: (value?: string) => set({ activeResponseName: value }),
  setListMappingStateResponse: (value?: IMapping[]) =>
    set({ listMappingStateResponse: value }),
  setSellerAPIExampleProps: (value: any) =>
    set({ sellerAPIExampleProps: value }),
  setActiveSonataResponse: (value?: string) =>
    set({ activeSonataResponse: value }),
  setListMappingStateRequest: (value?: IMapping[]) =>
    set({ listMappingStateRequest: value }),
  setErrors: (value) => set({ errors: value }),
}));
