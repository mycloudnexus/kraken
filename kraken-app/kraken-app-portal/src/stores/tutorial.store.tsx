import { storeData } from '@/utils/helpers/token';
import { create } from "zustand";

type TutorialStore = {
  tutorialCompleted?: boolean;
  openTutorial?: boolean
  setTutorialCompleted: (value: boolean) => void;
  setOpenTutorial: (value: boolean) => void;
  toggleTutorial(): void
  reset: () => void;
};

const defaultData = {
  tutorialCompleted: false,
  openTutorial: window.localStorage.getItem('tutorialCompleted') !== 'true',
};

export const useTutorialStore = create<TutorialStore>()((set) => ({
  ...defaultData,
  setTutorialCompleted: (tutorialCompleted) => {
    storeData('tutorialCompleted', 'true')
    set({ tutorialCompleted })
  },
  setOpenTutorial: (openTutorial) => set({ openTutorial }),
  reset: () => set(defaultData),
  toggleTutorial: () => set(state => ({ openTutorial: !state.openTutorial }))
}));
