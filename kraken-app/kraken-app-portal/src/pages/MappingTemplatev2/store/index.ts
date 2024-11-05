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
};

type TemplateMappingMutations = {
  setConfirmUpgrade(value: boolean): void;
  pushNotification(...notis: Noti[]): void;
  removeNotification(noti: Noti): void;
  clearNotification(): void;
  setIsMappingIncomplete(value: boolean): void;
  reset(): void;
};

const initialState: TemplateMappingState = {
  notification: [],
  confirmUpgrade: false,
  isMappingIncomplete: false,
};

export const useMappingTemplateStoreV2 = create<
  TemplateMappingState & TemplateMappingMutations
>((set) => ({
  ...initialState,
  // Mutations
  setConfirmUpgrade: (value) => set({ confirmUpgrade: value }),
  pushNotification: (...notis) =>
    set(state => ({ notification: [...state.notification, ...notis] })),
  removeNotification: (noti) =>
    set((state) => ({ notification: state.notification.filter(
      (prev) => prev.id !== noti.id
    ) })),
  clearNotification: () =>
    set(() => ({ notification: [] })),
  setIsMappingIncomplete: (value) => set({ isMappingIncomplete: value }),
  reset: () => set(initialState),
}));
