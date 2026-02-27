import { ENV } from "@/constants";
import { create } from "zustand";

type AppStore = {
  currentProduct: string;
  setCurrentProduct: (currentProduct: string) => void;
};

export const useAppStore = create<AppStore>()((set) => ({
  currentProduct: ENV.PRODUCT_ID,
  setCurrentProduct: (currentProduct: string) => set({ currentProduct }),
}));
