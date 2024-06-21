import StepBar from "@/components/StepBar";
import { useCreateNewComponent } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { EStep } from "@/utils/constants/common";
import { COMPONENT_KIND_API_TARGET_SPEC } from "@/utils/constants/product";
import { Form, notification } from "antd";
import { get, isEmpty } from "lodash";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AddEnv from "./components/AddEnv";
import BtnStep from "./components/BtnStep";
import SelectAPIServer from "./components/SelectAPIServer";
import SelectDownStreamAPI from "./components/SelectDownStreamAPI";
import styles from "./index.module.scss";
import PreviewAPIServer from "./components/PreviewAPIServer";

const NewAPIServer = () => {
  const [activeKey, setActiveKey] = useState<string | string[]>("0");
  const { currentProduct: id } = useAppStore();
  const [form] = Form.useForm();
  const [step, setStep] = useState(0);
  const { mutateAsync: runCreate, isPending: loadingCreate } =
    useCreateNewComponent();
  const navigate = useNavigate();

  const handleNext = async () => {
    try {
      if (step === 0) {
        await form.validateFields(["file", "name", "description", "link"]);
      }
      if (step === 2) {
        await form.validateFields([
          ["environments", "sit"],
          ["environments", "uat"],
          ["environments", "prod"],
          ["environments", "stage"],
        ]);
      }
      if (step === 3) {
        form.submit();
        return;
      }
      setActiveKey([(step + 1).toString()]);
      setStep(step + 1);
    } catch (error) {
      return;
    }
  };

  const handlePrev = () => {
    if (step === 0) {
      navigate(`/component/${id}/list`);
      return;
    }
    setActiveKey([(step - 1).toString()]);
    setStep(step - 1);
  };

  const onFinish = async (values: any) => {
    try {
      const swaggerData = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(values.file.file);
      });
      const data = {
        description: values.description,
        kind: COMPONENT_KIND_API_TARGET_SPEC,
        metadata: {
          name: values.name,
          version: 1,
          key: `mef.sonata.api-target-spec.${values.name
            ?.replace(" ", "")
            ?.substring(0, 3)
            .toLowerCase()}${new Date().getTime()}`,
          description: values.description,
        },
        spec: {
          baseSpec: {
            path: values.link,
            content: swaggerData,
          },
          selectedAPIs: values?.selectedAPIs,
          environments: values.environments,
        },
      };
      const res = await runCreate({
        productId: id,
        data,
      } as any);
      notification.success({ message: res.message });
      navigate(`/component/${id}/list`);
    } catch (error) {
      notification.error({
        message: get(error, "message", "Error. Please contact administration"),
      });
    }
  };

  const handleBack = (step: number) => {
    setStep(step);
    setActiveKey([step.toString()]);
  };

  return (
    <Form form={form} onFinish={onFinish}>
      <div className={styles.root}>
        <StepBar
          type={EStep.API_SERVER}
          currentStep={step}
          activeKey={activeKey}
          setActiveKey={setActiveKey}
        />
        <div className={styles.container}>
          <SelectAPIServer form={form} active={step === 0} />
          <AddEnv form={form} active={step === 2} />
          <SelectDownStreamAPI form={form} active={step === 1} />
          <PreviewAPIServer
            form={form}
            active={step === 3}
            handleBack={handleBack}
          />
          <Form.Item noStyle shouldUpdate>
            {({ getFieldValue }) => {
              const disabled =
                (isEmpty(getFieldValue("name")) ||
                  isEmpty(getFieldValue("file"))) &&
                step === 0;
              const disabledEnv =
                isEmpty(getFieldValue(["environments", "sit"])) &&
                isEmpty(getFieldValue(["environments", "prod"])) &&
                isEmpty(getFieldValue(["environments", "stage"])) &&
                isEmpty(getFieldValue(["environments", "uat"])) &&
                step == 2;
              return (
                <BtnStep
                  disabled={disabled || disabledEnv}
                  loading={loadingCreate}
                  onNext={handleNext}
                  onPrev={handlePrev}
                  currentStep={step}
                />
              );
            }}
          </Form.Item>
        </div>
      </div>
    </Form>
  );
};

export default NewAPIServer;
