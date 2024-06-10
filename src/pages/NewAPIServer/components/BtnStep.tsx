import { Button } from "antd";
import styles from "./index.module.scss";

type Props = {
  onNext: () => void;
  onPrev: () => void;
  currentStep: number;
};

const BtnStep = ({ onNext, onPrev, currentStep }: Props) => {
  return (
    <div className={styles.stepHandler}>
      <Button onClick={onPrev}>{currentStep === 0 ? "Cancel" : "Back"}</Button>
      <Button type="primary" shape="default" onClick={onNext}>
        {currentStep === 2 ? "Done" : "Next"}
      </Button>
    </div>
  );
};

export default BtnStep;
