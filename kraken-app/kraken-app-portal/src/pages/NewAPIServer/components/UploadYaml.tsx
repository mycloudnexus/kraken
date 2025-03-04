import TitleIcon from "@/assets/title-icon.svg";
import Flex from "@/components/Flex";
import SpecDrawer from "@/components/SpecDrawer";
import { Text } from "@/components/Text";
import { UploadOutlined, PaperClipOutlined } from "@ant-design/icons";
import { Upload, notification, Button, Form, FormInstance } from "antd";
import clsx from "clsx";
import { get } from "lodash";
import { useEffect, useState } from "react";
import { useBoolean } from "usehooks-ts";
import ReplaceFileModal from "./ReplaceFileModal";
import styles from "./index.module.scss";

type Props = {
  form: FormInstance<any>;
};

const uploadYamlFile = async (value: any) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = function (evt) {
      const content = evt.target?.result as string;

      const isOpenApi = content?.includes("openapi:");
      const isSwagger = content?.includes("swagger:");
      if (!isOpenApi && !isSwagger) {
        reject(new Error("Please upload valid open api spec in yaml format"));
      }

      resolve(true);
    };
    reader.onerror = reject;
    reader.readAsText(value.file, "utf-8");
  });
};

const UploadYaml = ({ form }: Props) => {
  const {
    value: isOpenDrawer,
    setTrue: openDrawer,
    setFalse: closeDrawer,
  } = useBoolean(false);
  const file = Form.useWatch("file", form);

  const [content, setContent] = useState("");
  const handleReplace = () => {
    closeModal();
    const fileSelector = document.getElementById("upload-file");
    if (fileSelector) {
      fileSelector.click();
    }
  };
  const {
    value: isOpenModal,
    setTrue: openModal,
    setFalse: closeModal,
  } = useBoolean(false);

  const loadFile = async () => {
    try {
      const swaggerData = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(file.file);
      });
      setContent(
        (swaggerData as string).replace(
          "data:application/octet-stream;base64",
          "data:application/x-yaml;base64"
        )
      );
    } catch (error) {
      form.setFieldValue("file", undefined);
      notification.error({
        message: "Please upload valid  open api spec in yaml format.",
      });
    }
  };

  useEffect(() => {
    if (file?.file) {
      loadFile();
    }
  }, [file?.file]);

  return (
    <div className={styles.uploadYamlRoot}>
      {isOpenDrawer && (
        <SpecDrawer
          onClose={closeDrawer}
          isOpen={isOpenDrawer}
          content={content}
        />
      )}
      <ReplaceFileModal
        isOpen={isOpenModal}
        onOk={handleReplace}
        onCancel={closeModal}
      />
      <Flex gap={8} justifyContent="flex-start" style={{ marginBottom: 10 }}>
        <TitleIcon />
        <Text.NormalLarge>API spec </Text.NormalLarge>
        <span style={{ color: "#FF4D4F" }}>*</span>
      </Flex>
      <Form.Item
        className={clsx([styles.hideRequired, styles.hideError])}
        name="file"
        label="Upload API Spec in yaml format :"
        rules={[
          { required: true, message: "Please upload API spec." },
          () => ({
            validator: async (_, value) => {
              if (!file?.file) {
                return Promise.resolve();
              }

              return uploadYamlFile(value);
            },
          }),
        ]}
        style={{ display: file?.file ? "none" : "block" }}
      >
        <Upload
          id="upload-file"
          accept=".yaml"
          showUploadList={false}
          multiple={false}
          beforeUpload={(file) => {
            if (!/^.*\.yaml$/.test(file.name)) {
              notification.warning({ message: "Only accept yaml file" });
            }
            form.setFieldValue("selectedAPIs", []);
            return false;
          }}
        >
          <Button data-testid="btnUpload" icon={<UploadOutlined />}>
            Click to upload
          </Button>
        </Upload>
      </Form.Item>

      <Form.Item
        className={styles.hideRequired}
        label="Upload API Spec in yaml format :"
        style={{ display: !file?.file ? "none" : "block" }}
      >
        <Button
          data-testid="btnUploadReplace"
          icon={<UploadOutlined />}
          onClick={openModal}
        >
          Click to upload
        </Button>
      </Form.Item>
      {file ? (
        <Flex gap={9} justifyContent="flex-start">
          <PaperClipOutlined />
          <Text.LightMedium>{get(file, "file.name", "")}</Text.LightMedium>
          <Text.LightMedium
            data-testid="btnViewFileContent"
            color="#2962FF"
            role="none"
            style={{ cursor: "pointer" }}
            onClick={openDrawer}
          >
            View
          </Text.LightMedium>
        </Flex>
      ) : null}
      <Form.Item shouldUpdate>
        {({ getFieldError }) => {
          const errors = getFieldError("file");
          return (
            <div>
              {errors?.map((e) => (
                <div key={e} className="ant-form-item-explain-error">
                  {e}
                </div>
              ))}
            </div>
          );
        }}
      </Form.Item>
    </div>
  );
};

export default UploadYaml;
