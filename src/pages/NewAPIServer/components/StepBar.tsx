import StepIcon from "@/assets/stepstart.svg";
import Text from "@/components/Text";
import {
  CheckCircleFilled,
  CloseOutlined,
  DownOutlined,
  MinusOutlined,
  UpOutlined,
} from "@ant-design/icons";
import { Button, Collapse, CollapseProps } from "antd";
import clsx from "clsx";
import { CSSProperties, useRef, useState } from "react";
import type { DraggableData, DraggableEvent } from "react-draggable";
import Draggable from "react-draggable";
import styles from "./index.module.scss";
import Flex from "@/components/Flex";

type Props = {
  currentStep: number;
  activeKey: string | string[];
  setActiveKey: (activeKey: string | string[]) => void;
};

interface IStepTitle {
  activeKey: string | string[];
  stepKey: string;
  isFinished: boolean;
  content: string;
  isCurrentStep?: boolean;
}

interface IStepIndicator {
  currentStep: number;
}

const StepTitle = ({
  activeKey,
  stepKey,
  isFinished,
  content,
  isCurrentStep,
}: IStepTitle) => {
  return (
    <div
      className={clsx(styles.collapseTitle, {
        [styles.active]: activeKey === stepKey,
      })}
    >
      {isFinished ? (
        <CheckCircleFilled style={{ color: "#389E0D" }} />
      ) : (
        <CheckCircleFilled
          style={{ color: isCurrentStep ? "#2962FF" : "#DDE1E5" }}
        />
      )}
      <span style={{ paddingLeft: 8 }}> {content}</span>
    </div>
  );
};

const StepIndicator = ({ currentStep }: IStepIndicator) => {
  return (
    <div style={{ display: "flex", flexDirection: "row", marginBottom: 16 }}>
      <div
        className={clsx(styles.stepIndicator, {
          [styles.stepIndicatorActive]: currentStep === 0,
        })}
      />
      <div
        className={clsx(styles.stepIndicator, {
          [styles.stepIndicatorActive]: currentStep === 1,
        })}
      />
      <div
        className={clsx(styles.stepIndicator, {
          [styles.stepIndicatorActive]: currentStep === 2,
        })}
      />
    </div>
  );
};

const getItems: (
  panelStyle: CSSProperties,
  panelStyleActive: CSSProperties,
  currentStep: number,
  activeKey: string | string[]
) => CollapseProps["items"] = (
  panelStyle,
  panelStyleActive,
  currentStep,
  activeKey
) => [
  {
    key: "0",
    label: (
      <StepTitle
        activeKey={activeKey}
        isCurrentStep={currentStep === 0}
        stepKey="0"
        content="1. Basic Information of API server"
        isFinished={currentStep > 0}
      />
    ),
    children: (
      <Text.LightMedium>
        Add basic information to your API server and upload API spec, with which
        the system can abstract your API list.
      </Text.LightMedium>
    ),
    style: activeKey === "0" ? panelStyleActive : panelStyle,
  },
  {
    key: "1",
    label: (
      <StepTitle
        isCurrentStep={currentStep === 1}
        activeKey={activeKey}
        stepKey="1"
        content="2. Select Seller APIs"
        isFinished={currentStep > 1}
      />
    ),
    children: (
      <Text.LightMedium>
        Select seller APIs from seller API spec list which will be used in
        Sonata API mapping, and add to the right.
        <br />
        <br />
        You can add multiple APIs to the right side.
      </Text.LightMedium>
    ),
    style: activeKey === "1" ? panelStyleActive : panelStyle,
  },
  {
    key: "2",
    label: (
      <StepTitle
        isCurrentStep={currentStep === 2}
        activeKey={activeKey}
        stepKey="2"
        content="3. Add environment Variables"
        isFinished={currentStep > 2}
      />
    ),
    children: (
      <Text.LightMedium>Select the environments and add URLs.</Text.LightMedium>
    ),
    style: activeKey === "2" ? panelStyleActive : panelStyle,
  },
];

const StepBar = ({ currentStep = 0, activeKey, setActiveKey }: Props) => {
  const [isOpen, setIsOpen] = useState(true);
  const [isStart, setIsStart] = useState(false);
  const [bounds, setBounds] = useState({
    left: 0,
    top: 0,
    bottom: 0,
    right: 0,
  });
  const draggleRef = useRef<HTMLDivElement>(null);

  const panelStyle: React.CSSProperties = {
    marginBottom: 24,
    borderRadius: 4,
    border: "1px solid #DDE1E5",
  };
  const panelStyleActive: React.CSSProperties = {
    marginBottom: 24,
    borderRadius: 4,
    border: "1px solid #2962FF",
  };

  const onStart = (_event: DraggableEvent, uiData: DraggableData) => {
    const { clientWidth, clientHeight } = window.document.documentElement;
    const targetRect = draggleRef.current?.getBoundingClientRect();
    if (!targetRect) {
      return;
    }
    setBounds({
      left: -targetRect.left + uiData.x,
      right: clientWidth - (targetRect.right - uiData.x),
      top: -targetRect.top + uiData.y,
      bottom: clientHeight - (targetRect.bottom - uiData.y),
    });
  };

  const onChange = (key: string | string[]) => {
    setActiveKey(key);
  };

  return (
    <Draggable
      bounds={bounds}
      nodeRef={draggleRef}
      onStart={(event, uiData) => onStart(event, uiData)}
    >
      <div
        ref={draggleRef}
        className={clsx(styles.draggableModal, {
          [styles.hiddenModal]: !isOpen,
        })}
      >
        <div className={styles.barHeader}>
          <Text.Custom size="20px" bold="500">
            Seller API Setup
          </Text.Custom>
          <Flex justifyContent="flex-end" gap={12}>
            <MinusOutlined
              onClick={() => setIsOpen(false)}
              style={{ color: "#00000073" }}
            />
            <CloseOutlined
              onClick={() => setIsOpen(false)}
              style={{ color: "#00000073" }}
            />
          </Flex>
        </div>
        <div className={styles.barContent}>
          {isStart && <StepIndicator currentStep={currentStep} />}

          {!isStart ? (
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                flexDirection: "column",
              }}
            >
              <p style={{ marginBottom: 39, fontWeight: 500, fontSize: 20 }}>
                Starting with seller API setup
              </p>
              <StepIcon />
              <Button
                style={{ marginTop: 71 }}
                type="primary"
                shape="default"
                onClick={() => setIsStart(true)}
              >
                Start the tutorial
              </Button>
            </div>
          ) : (
            <div>
              <Collapse
                activeKey={activeKey}
                items={getItems(
                  panelStyle,
                  panelStyleActive,
                  currentStep,
                  activeKey[0]
                )}
                bordered={false}
                style={{ backgroundColor: "white" }}
                expandIcon={({ isActive }) =>
                  !isActive ? <DownOutlined /> : <UpOutlined />
                }
                expandIconPosition="end"
                accordion
                defaultActiveKey={["0"]}
                onChange={onChange}
              />
            </div>
          )}
        </div>
      </div>
    </Draggable>
  );
};

export default StepBar;
