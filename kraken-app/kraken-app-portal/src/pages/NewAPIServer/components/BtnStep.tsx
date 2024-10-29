import { ExclamationCircleFilled } from "@ant-design/icons";
import { Button, Modal } from "antd";
import { useNavigate, useParams } from "react-router-dom";
import styles from "./index.module.scss";

type Props = {
  onNext: () => void;
  disabled?: boolean;
  loading?: boolean;
};

const { confirm } = Modal;

const BtnStep = ({
  children,
  onNext,
  disabled,
  loading,
}: React.PropsWithChildren<Props>) => {
  const { componentId } = useParams();
  const navigate = useNavigate();
  const showConfirm = () => {
    if (componentId) {
      navigate(-1);
      return;
    }
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

      {children}
      <Button
        type="primary"
        shape="default"
        onClick={onNext}
        disabled={loading || disabled}
        loading={loading}
      >
        Save
      </Button>
    </div>
  );
};

export default BtnStep;
