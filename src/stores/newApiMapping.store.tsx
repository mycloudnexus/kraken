import { create } from "zustand";

type NewApiMappingStore = {
  query: string | undefined;
  setQuery: (q: string) => void;
};

export const useNewApiMappingStore = create<NewApiMappingStore>()((set) => ({
  query: undefined,
  setQuery: (query: string) => set({ query }),
}));
