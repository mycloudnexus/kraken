import { create } from "zustand";

export interface IUserParams {
  q?: string;
  page?: number;
  size?: number;
  filterInternalUser?: boolean;
}

type UserStore = {
  userParams: IUserParams;
  setUserParams: (userParams: Partial<IUserParams>) => void;
  resetParams: () => void;
};

export const defaultData = {
  userParams: {
    q: undefined,
    page: 0,
    size: 10,
    filterInternalUser: true
  },
};

export const useUserStore = create<UserStore>((set) => ({
  ...defaultData,
  setUserParams: (data) =>
    set((state) => ({ userParams: { ...state.userParams, ...data } })),
  resetParams: () => set(() => ({ userParams: defaultData.userParams })),
}));
