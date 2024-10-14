import { DEFAULT_PRODUCT } from "@/utils/constants/product";
import { create } from "zustand";

type AppStore = {
  currentProduct: string;
  setCurrentProduct: (currentProduct: string) => void;
};

export const useAppStore = create<AppStore>()((set) => ({
  currentProduct: DEFAULT_PRODUCT,
  setCurrentProduct: (currentProduct: string) => set({ currentProduct }),
}));
