
import { create } from "zustand";

type MappingUiStore = {
  activePath?: string;
  selectedKey: string;
  activeTab?: string;
  setActivePath: (activePath: string | undefined) => void;
  setSelectedKey: (selectedKey: string) => void;
  setActiveTab: (activeTab: string) => void;
  resetUiStore: () => void;
};

const defaultData = {
  activePath: undefined,
  selectedKey: '',
  activeTab: 'request',
};

export const useMappingUiStore = create<MappingUiStore>()((set) => ({
  ...defaultData,
  setActivePath: (activePath: string | undefined) => set({ activePath }),
  setSelectedKey: (selectedKey: string) => set({ selectedKey }),
  setActiveTab: (activeTab: string) => set({ activeTab }),
  resetUiStore: () => set(defaultData),
}));
