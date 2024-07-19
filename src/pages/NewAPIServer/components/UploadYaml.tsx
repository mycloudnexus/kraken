import Text from "@/components/Text";
import { UploadOutlined, PaperClipOutlined } from "@ant-design/icons";
import { Upload, notification, Button, Form, FormInstance } from "antd";
import { get } from "lodash";
import ReplaceFileModal from "./ReplaceFileModal";
import { useBoolean } from "usehooks-ts";
import Flex from "@/components/Flex";
import TitleIcon from "@/assets/title-icon.svg";
import styles from "./index.module.scss";
import SpecDrawer from "@/components/SpecDrawer";
import { useEffect, useState } from "react";

type Props = {
  form: FormInstance<any>;
};

export const tranformSwaggerToArray = (data: any) => {
  const paths = data.paths;
  const pathsArray = [];

  for (const path in paths) {
    for (const method in paths[path]) {
      const pathObject = {
        key: `${path} ${method}`,
        title: path,
        description: method,
        info: paths[path][method],
      };
      pathsArray.push(pathObject);
    }
  }

  return pathsArray;
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
      notification.error({ message: "Please select a valid swagger file" });
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
        className={styles.hideRequired}
        name="file"
        label="Upload API Spec in yaml format :"
        rules={[{ required: true, message: "Please upload API spec." }]}
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
          <Button icon={<UploadOutlined />}>Click to upload</Button>
        </Upload>
      </Form.Item>
      <Form.Item
        className={styles.hideRequired}
        label="Upload API Spec in yaml format :"
        style={{ display: !file?.file ? "none" : "block" }}
      >
        <Button icon={<UploadOutlined />} onClick={openModal}>
          Click to upload
        </Button>
      </Form.Item>

      {file ? (
        <Flex gap={9} justifyContent="flex-start">
          <PaperClipOutlined />
          <Text.LightMedium>{get(file, "file.name", "")}</Text.LightMedium>
          <Text.LightMedium
            color="#2962FF"
            role="none"
            style={{ cursor: "pointer" }}
            onClick={openDrawer}
          >
            View
          </Text.LightMedium>
        </Flex>
      ) : null}
    </div>
  );
};

export default UploadYaml;
