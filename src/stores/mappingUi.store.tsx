
import { create } from "zustand";

type MappingUiStore = {
  activePath?: string;
  selectedKey: string;
  activeTab?: string;
  mappingInProgress: boolean;
  setActivePath: (activePath: string | undefined) => void;
  setSelectedKey: (selectedKey: string) => void;
  setActiveTab: (activeTab: string) => void;
  setMappingInProgress: (value: boolean) => void;
  resetUiStore: () => void;
};

const defaultData = {
  activePath: undefined,
  selectedKey: '',
  activeTab: 'request',
  mappingInProgress: false,
};

export const useMappingUiStore = create<MappingUiStore>()((set) => ({
  ...defaultData,
  setActivePath: (activePath: string | undefined) => set({ activePath }),
  setSelectedKey: (selectedKey: string) => set({ selectedKey }),
  setActiveTab: (activeTab: string) => set({ activeTab }),
  setMappingInProgress: (mappingInProgress: boolean) => set({ mappingInProgress }),
  resetUiStore: () => set(defaultData),
}));
