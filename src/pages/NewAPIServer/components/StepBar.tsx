import styles from "./index.module.scss";
import StepIcon from "@/assets/stepstart.svg";
import ETIcon from "@/assets/et.svg";
import { Avatar, Button, Divider } from "antd";
import clsx from "clsx";
import { DoubleLeftOutlined } from "@ant-design/icons";
import Text from "@/components/Text";

type Props = {
  currentStep: number;
};

const Step = ({ active = false, step = "1", content = "" }) => {
  return (
    <div className={clsx(styles.step, { [styles.active]: active })}>
      <Avatar size={51}>{step}</Avatar>
      <Text.NormalMedium color="#595959">{content}</Text.NormalMedium>
    </div>
  );
};

const StepBar = ({ currentStep = 0 }: Props) => {
  return (
    <div className={styles.bar}>
      <div className={styles.barTop}>
        <StepIcon />
        <Text.Custom size="24px" style={{ marginTop: 22 }}>
          Let’s get you start!
        </Text.Custom>
        <div className={styles.stepBar}>
          <Step
            active={currentStep === 0}
            step="1"
            content="Select your API server"
          />
          <Step
            active={currentStep === 1}
            step="2"
            content="Select downstream API"
          />
          <Step active={currentStep === 2} step="3" content="Add environment" />
        </div>
      </div>
      <div>
        <div className={styles.version}>
          <Text.LightSmall color="#00000040">
            V1.0 | A product by
          </Text.LightSmall>
          <ETIcon />
        </div>
        <Divider style={{ margin: "16px 0" }} />
        <div className={styles.collapseBtn}>
          <Button style={{ borderColor: "#D9D9D9" }}>
            <DoubleLeftOutlined style={{ color: "#D9D9D9" }} />
          </Button>
        </div>
      </div>
    </div>
  );
};

export default StepBar;
