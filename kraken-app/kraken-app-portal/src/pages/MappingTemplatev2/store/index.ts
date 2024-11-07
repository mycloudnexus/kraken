import { nanoid } from "nanoid";
import { create } from "zustand";

type Noti = {
  id: string;
  message: string;
  type: "success" | "error" | "warning" | "info";
};

type TemplateMappingState = {
  notification: Array<Noti>;
  confirmUpgrade: boolean;
  isMappingIncomplete: boolean;
  isStageMappingIncompatible: boolean;
  isProductionMappingIncompatible: boolean
};

type TemplateMappingMutations = {
  setConfirmUpgrade(value: boolean): void;
  pushNotification(...notis: Omit<Noti, 'id'>[]): void;
  removeNotification(noti: Noti): void;
  clearNotification(): void;
  setIsMappingIncomplete(value: boolean): void;
  setIsStageMappingIncompatible(value: boolean): void;
  setIsProductionMappingIncompatible(value: boolean): void;
  reset(): void;
};

const initialState: TemplateMappingState = {
  notification: [],
  confirmUpgrade: false,
  isMappingIncomplete: false,
  isStageMappingIncompatible: false,
  isProductionMappingIncompatible: false,
};

export const useMappingTemplateStoreV2 = create<
  TemplateMappingState & TemplateMappingMutations
>((set) => ({
  ...initialState,
  // Mutations
  setConfirmUpgrade: (value) => set({ confirmUpgrade: value }),
  pushNotification: (...notis) =>
    set(state => ({ notification: [...state.notification, ...notis.map(noti => ({ ...noti, id: nanoid() }))] })),
  removeNotification: (noti) =>
    set((state) => ({
      notification: state.notification.filter(
        (prev) => prev.id !== noti.id
      )
    })),
  clearNotification: () =>
    set(() => ({ notification: [] })),
  setIsMappingIncomplete: (value) => set({ isMappingIncomplete: value }),
  setIsStageMappingIncompatible: (value) => set({ isStageMappingIncompatible: value }),
  setIsProductionMappingIncompatible: (value) => set({ isProductionMappingIncompatible: value }),
  reset: () => set(initialState),
}));
