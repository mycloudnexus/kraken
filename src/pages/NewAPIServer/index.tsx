import { useState } from "react";
import styles from "./index.module.scss";
import StepBar from "./components/StepBar";
import SelectAPIServer from "./components/SelectAPIServer";
import { Form, notification } from "antd";
import AddEnv from "./components/AddEnv";
import SelectDownStreamAPI from "./components/SelectDownStreamAPI";
import BtnStep from "./components/BtnStep";
import { API_SERVER_KEY } from "@/utils/constants/product";
import { useCreateNewComponent } from "@/hooks/product";
import { get } from "lodash";
import { useAppStore } from "@/stores/app.store";
import { useNavigate } from "react-router-dom";

const NewAPIServer = () => {
  const { currentProduct: id } = useAppStore();
  const [form] = Form.useForm();
  const [step, setStep] = useState(0);
  const { mutateAsync: runCreate } = useCreateNewComponent();
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
      if (step === 2) {
        form.submit();
        return;
      }
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
        kind: API_SERVER_KEY,
        metadata: {
          name: values.name,
          version: 1,
          key: `mef.sonata.api-target-spec.${values.name
            ?.replace(" ", "")
            ?.substring(0, 4)
            .toLowerCase()}`,
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

  return (
    <Form form={form} onFinish={onFinish}>
      <div className={styles.root}>
        <StepBar currentStep={step} />
        <div className={styles.container}>
          <SelectAPIServer form={form} active={step === 0} />
          <AddEnv form={form} active={step === 2} />
          <SelectDownStreamAPI
            form={form}
            active={step === 1}
            onNext={handleNext}
            onPrev={handlePrev}
            currentStep={step}
          />
          {step !== 1 && (
            <BtnStep
              onNext={handleNext}
              onPrev={handlePrev}
              currentStep={step}
            />
          )}
        </div>
      </div>
    </Form>
  );
};

export default NewAPIServer;
