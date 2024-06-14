import Text from "@/components/Text";
import styles from "./index.module.scss";
import { Form, Input, Upload, Button, FormInstance, notification } from "antd";
import {
  InboxOutlined,
  PaperClipOutlined,
  UploadOutlined,
} from "@ant-design/icons";
import Flex from "@/components/Flex";
import { get, isEmpty } from "lodash";
import { useBoolean } from "usehooks-ts";
import ReplaceFileModal from "./ReplaceFileModal";
import { isURL } from "@/utils/helpers/url";

const { Dragger } = Upload;

type Props = {
  form: FormInstance<any>;
  active: boolean;
};

const SelectAPIServer = ({ form, active }: Props) => {
  const {
    value: isOpenModal,
    setTrue: openModal,
    setFalse: closeModal,
  } = useBoolean(false);
  const file = Form.useWatch("file", form);
  const handleReplace = async () => {
    await form.setFieldValue("file", null);
    await form.setFieldValue("selectedAPIs", []);
    closeModal();
    const fileSelector = document.getElementById("upload-file");
    if (fileSelector) {
      fileSelector.click();
    }
  };

  return (
    <div
      style={{
        display: active ? "flex" : "none",
        flex: 1,
        flexDirection: "column",
      }}
    >
      <ReplaceFileModal
        isOpen={isOpenModal}
        onOk={handleReplace}
        onCancel={closeModal}
      />
      <Text.BoldLarge>Add information for the API server</Text.BoldLarge>
      <div className={styles.paper} style={{ flex: 1 }}>
        <Form.Item
          label="API server name"
          name="name"
          rules={[
            {
              required: true,
              message: "Please fill the application name",
            },
          ]}
          labelCol={{ span: 24 }}
        >
          <Input placeholder="Add application name" style={{ width: "50%" }} />
        </Form.Item>
        <Form.Item
          label="Description"
          name="description"
          labelCol={{ span: 24 }}
        >
          <Input placeholder="Add description" style={{ width: "50%" }} />
        </Form.Item>
        <Form.Item
          label="Online API document link"
          name="link"
          rules={[
            {
              required: false,
            },
            {
              validator: (_, value) => {
                if (isURL(value) || isEmpty(value)) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error("Please enter a valid URL"));
              },
            },
          ]}
          labelCol={{ span: 24 }}
          validateTrigger="onBlur"
        >
          <Input placeholder="Add link" style={{ width: "50%" }} />
        </Form.Item>
        <Form.Item
          name="file"
          label="Upload API Spec in yaml format:"
          rules={[{ required: true, message: "Please upload a file" }]}
        >
          {isEmpty(file?.file) ? (
            <Upload
              id="upload-file"
              accept=".yaml"
              showUploadList={false}
              multiple={false}
              beforeUpload={(file) => {
                if (!/^.*\.yaml$/.test(file.name)) {
                  notification.warning({ message: "Only accept yaml file" });
                }
                return false;
              }}
            >
              <Button icon={<UploadOutlined />}>Click to upload</Button>
            </Upload>
          ) : (
            <Button icon={<UploadOutlined />} onClick={openModal}>
              Click to upload
            </Button>
          )}
        </Form.Item>
        {isEmpty(file?.file) && (
          <Form.Item noStyle name="file">
            <Dragger
              accept=".yaml"
              multiple={false}
              showUploadList={false}
              className={styles.dragger}
              beforeUpload={(file) => {
                if (!/^.*\.yaml$/.test(file.name)) {
                  notification.warning({ message: "Only accept yaml file" });
                }
                return false;
              }}
            >
              <p className="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p className="ant-upload-text">
                Click or drag file to this area to upload
              </p>
              <p className="ant-upload-hint">
                Support for a single or bulk upload. Strictly prohibit from
                uploading company data or other band files
              </p>
            </Dragger>
          </Form.Item>
        )}
        {file ? (
          <Flex gap={9} justifyContent="flex-start">
            <PaperClipOutlined />
            <Text.LightMedium>{get(file, "file.name", "")}</Text.LightMedium>
          </Flex>
        ) : null}
      </div>
    </div>
  );
};

export default SelectAPIServer;
