import Text from "@/components/Text";
import styles from "./index.module.scss";
import { Col, Form, Row, Input, Upload, Button, FormInstance } from "antd";
import { PaperClipOutlined, UploadOutlined } from "@ant-design/icons";
import Flex from "@/components/Flex";
import { get, isEmpty } from "lodash";
import { useBoolean } from "usehooks-ts";
import ReplaceFileModal from "./ReplaceFileModal";
import { isURL } from "@/utils/helpers/url";

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
    <div style={{ display: active ? "block" : "none" }}>
      <ReplaceFileModal
        isOpen={isOpenModal}
        onOk={handleReplace}
        onCancel={closeModal}
      />
      <Text.BoldLarge>Add information for the API server</Text.BoldLarge>
      <div className={styles.paper}>
        <Row gutter={[40, 16]} style={{ width: "100%" }}>
          <Col span={8}>
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
              <Input
                placeholder="Add application name"
                style={{ width: "100%" }}
              />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item
              label="Description"
              name="description"
              labelCol={{ span: 24 }}
            >
              <Input placeholder="Add description" style={{ width: "100%" }} />
            </Form.Item>
          </Col>
          <Col span={8}>
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
                    return Promise.reject(
                      new Error("Please enter a valid URL")
                    );
                  },
                },
              ]}
              labelCol={{ span: 24 }}
              validateTrigger="onBlur"
            >
              <Input placeholder="Add link" style={{ width: "100%" }} />
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item
              name="file"
              label="Upload API Spec:"
              rules={[{ required: true, message: "Please upload a file" }]}
            >
              {isEmpty(file?.file) ? (
                <Upload
                  id="upload-file"
                  accept=".yaml"
                  showUploadList={false}
                  multiple={false}
                  beforeUpload={() => false}
                >
                  <Button icon={<UploadOutlined />}>Click to upload</Button>
                </Upload>
              ) : (
                <Button icon={<UploadOutlined />} onClick={openModal}>
                  Click to upload
                </Button>
              )}
            </Form.Item>
          </Col>
          <Col span={24} style={{ marginTop: -16 }}>
            {file ? (
              <Flex gap={9} justifyContent="flex-start">
                <PaperClipOutlined />
                <Text.LightMedium>
                  {get(file, "file.name", "")}
                </Text.LightMedium>
              </Flex>
            ) : null}
          </Col>
        </Row>
      </div>
    </div>
  );
};

export default SelectAPIServer;
