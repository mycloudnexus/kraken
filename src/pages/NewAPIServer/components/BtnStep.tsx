import { Button } from "antd";
import styles from "./index.module.scss";

type Props = {
  onNext: () => void;
  onPrev: () => void;
  currentStep: number;
  disabled?: boolean;
  loading?: boolean;
};

const BtnStep = ({ onNext, onPrev, currentStep, disabled, loading }: Props) => {
  return (
    <div className={styles.stepHandler}>
      <Button onClick={onPrev}>{currentStep === 0 ? "Cancel" : "Back"}</Button>
      <Button
        type="primary"
        shape="default"
        onClick={onNext}
        disabled={loading || disabled}
        loading={loading}
      >
        {currentStep === 2 ? "Done" : "Next"}
      </Button>
    </div>
  );
};

export default BtnStep;
