
import { create } from "zustand";

type MappingUiStore = {
  activePath?: string;
  selectedKey: string;
  activeTab?: string;
  setActivePath: (activePath: string) => void;
  setSelectedKey: (selectedKey: string) => void;
  setActiveTab: (activeTab: string) => void;
  reset: () => void;
};

const defaultData = {
  activePath: undefined,
  selectedKey: '',
  activeTab: 'request',
};

export const useMappingUiStore = create<MappingUiStore>()((set) => ({
  ...defaultData,
  setActivePath: (activePath: string) => set({ activePath }),
  setSelectedKey: (selectedKey: string) => set({ selectedKey }),
  setActiveTab: (activeTab: string) => set({ activeTab }),
  reset: () => set(defaultData),
}));
