import { Button, Modal } from "antd";
import styles from "./index.module.scss";
import { useNavigate } from "react-router-dom";
import { ExclamationCircleFilled } from "@ant-design/icons";

type Props = {
  onNext: () => void;
  onPrev: () => void;
  currentStep: number;
  disabled?: boolean;
  loading?: boolean;
};

const { confirm } = Modal;

const BtnStep = ({ onNext, onPrev, currentStep, disabled, loading }: Props) => {
  const navigate = useNavigate();
  const showConfirm = () => {
    confirm({
      title: "Are you sure to cancel the seller API setup?",
      icon: <ExclamationCircleFilled />,
      content:
        "Continue will go back the home page without saving any content.",
      onOk() {
        navigate(-1);
      },
      okText: "Continue",
      okButtonProps: {
        style: {
          background: "#FF4D4F",
        },
      },
    });
  };

  return (
    <div className={styles.stepHandler}>
      <Button onClick={showConfirm} type="text">
        Cancel
      </Button>
      {currentStep > 0 && <Button onClick={onPrev}>Previous</Button>}
      <Button
        type="primary"
        shape="default"
        onClick={onNext}
        disabled={loading || disabled}
        loading={loading}
      >
        {currentStep === 3 ? "Save and exit" : "Next"}
      </Button>
    </div>
  );
};

export default BtnStep;
