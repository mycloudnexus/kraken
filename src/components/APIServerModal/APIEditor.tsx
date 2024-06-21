import {
  Button,
  Checkbox,
  Col,
  Form,
  Input,
  Row,
  Upload,
  notification,
} from "antd";
import Text from "../Text";
import { PaperClipOutlined, UploadOutlined } from "@ant-design/icons";
import Flex from "../Flex";
import { cloneDeep, get, isEmpty, set } from "lodash";
import { isURL } from "@/utils/helpers/url";
import { useEffect, useState } from "react";
import { decode } from "js-base64";
import jsYaml from "js-yaml";
import styles from "./index.module.scss";
import { useEditComponent } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";

type Props = {
  detail: any;
  onClose?: () => void;
  refresh?: () => void;
  componentId: string;
};

const APIEditor = ({ detail, onClose, refresh, componentId }: Props) => {
  const { currentProduct } = useAppStore();
  const { mutateAsync: runUpdate, isPending } = useEditComponent();
  const [form] = Form.useForm();
  const [isChangedFile, setIsChangedFile] = useState(false);
  const file = Form.useWatch("file", form);

  const isSIT = Form.useWatch("isSIT", form);
  const isProd = Form.useWatch("isProd", form);
  const isStage = Form.useWatch("isStage", form);
  const isUat = Form.useWatch("isUat", form);

  useEffect(() => {
    try {
      if (isEmpty(detail)) {
        return;
      }
      const base64data = get(detail, "facets.baseSpec.content");
      let swaggerData;
      let fileDecode = "";
      if (base64data) {
        fileDecode = decode(get(detail, "facets.baseSpec.content"));
        swaggerData = jsYaml.load(fileDecode);
      }

      form.setFieldsValue({
        name: get(detail, "metadata.name"),
        description: get(detail, "metadata.description"),
        link: get(detail, "facets.baseSpec.path"),
        file: {
          file: isEmpty(base64data)
            ? undefined
            : new File(
                [fileDecode],
                `${get(swaggerData, "info.title", "file")}.yaml`
              ),
        },
        isSIT: !!get(detail, "facets.environments.sit"),
        isProd: !!get(detail, "facets.environments.prod"),
        isStage: !!get(detail, "facets.environments.stage"),
        isUat: !!get(detail, "facets.environments.uat"),
        environments: get(detail, "facets.environments"),
      });
    } catch (error) {
      notification.error({ message: "Error. Please try again" });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [detail]);

  const onFinish = async (values: any) => {
    try {
      const swaggerData = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(values.file.file);
      });
      const data = cloneDeep(detail);
      set(data, "metadata.name", values.name);
      set(data, "metadata.description", values.description);
      set(data, "facets.baseSpec.path", values.link);
      set(data, "facets.environments", values.environments);
      if (isChangedFile) {
        set(data, "facets.selectedAPIs", []);
        set(data, "facets.baseSpec.content", swaggerData);
      }
      set(data, "metadata.version", get(data, "metadata.version", 1) + 1);
      await runUpdate({ productId: currentProduct, componentId, data } as any);
      refresh?.();
      notification.success({ message: "Edit success" });
      onClose?.();
    } catch (error) {
      notification.error({
        message: get(error, "message", "Error. Please try again"),
      });
    }
  };

  return (
    <div>
      <Text.BoldLarge>Edit API server</Text.BoldLarge>
      <Form form={form} style={{ marginTop: 24 }} onFinish={onFinish}>
        <Row gutter={[32, 8]} style={{ width: "100%" }}>
          <Col span={8}>
            <Form.Item
              name="name"
              label="API server name"
              labelCol={{ span: 24 }}
              rules={[
                {
                  required: true,
                  message: "Please fill the application name",
                },
              ]}
            >
              <Input
                style={{ width: "100%" }}
                placeholder="Add application name"
              />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item
              labelCol={{ span: 24 }}
              name="description"
              label="Description"
            >
              <Input placeholder="Add description" style={{ width: "100%" }} />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item
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
              label="Online API document link"
              validateTrigger="onBlur"
              labelCol={{ span: 24 }}
            >
              <Input placeholder="Add link" style={{ width: "100%" }} />
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item
              label="Upload API Spec:"
              name="file"
              rules={[{ required: true, message: "Please upload a file" }]}
            >
              <Upload
                accept=".yaml"
                id="upload-file"
                showUploadList={false}
                multiple={false}
                beforeUpload={() => {
                  setIsChangedFile(true);
                  return false;
                }}
              >
                <Button icon={<UploadOutlined />}>Click to upload</Button>
              </Upload>
            </Form.Item>
          </Col>
          <Col span={24} style={{ marginTop: -16 }}>
            {file?.file ? (
              <Flex gap={9} justifyContent="flex-start">
                <PaperClipOutlined />
                <Text.LightMedium>
                  {get(file, "file.name", "")}
                </Text.LightMedium>
                <Button
                  type="text"
                  style={{ color: "#2962FF" }}
                  onClick={() => form.setFieldValue("file", undefined)}
                >
                  Delete
                </Button>
              </Flex>
            ) : null}
          </Col>
          <Col span={24}>
            <Text.LightLarge>
              Environment Variables <span style={{ color: "#FF4D4F" }}>*</span>
            </Text.LightLarge>
          </Col>
          <Col span={4}>
            <Form.Item name="isSIT" valuePropName="checked">
              <Checkbox>Development</Checkbox>
            </Form.Item>
          </Col>
          <Col span={20}>
            <Form.Item
              label="URL:"
              name={["environments", "sit"]}
              style={{ width: "60%" }}
              rules={[{ required: isSIT, message: "Please fill the url" }]}
            >
              <Input placeholder="Add URL" disabled={!isSIT} />
            </Form.Item>
          </Col>
          <Col span={4}>
            <Form.Item name="isProd" valuePropName="checked">
              <Checkbox>Production</Checkbox>
            </Form.Item>
          </Col>
          <Col span={20}>
            <Form.Item
              name={["environments", "prod"]}
              style={{ width: "60%" }}
              label="URL:"
              rules={[{ required: isProd, message: "Please fill the url" }]}
            >
              <Input placeholder="Add URL" disabled={!isProd} />
            </Form.Item>
          </Col>
          <Col span={4}>
            <Form.Item valuePropName="checked" name="isStage">
              <Checkbox>Stage</Checkbox>
            </Form.Item>
          </Col>
          <Col span={20}>
            <Form.Item
              label="URL:"
              name={["environments", "stage"]}
              rules={[{ required: isStage, message: "Please fill the url" }]}
              style={{ width: "60%" }}
            >
              <Input placeholder="Add URL" disabled={!isStage} />
            </Form.Item>
          </Col>
          <Col span={4}>
            <Form.Item name="isUat" valuePropName="checked">
              <Checkbox>UAT</Checkbox>
            </Form.Item>
          </Col>
          <Col span={20}>
            <Form.Item
              rules={[{ required: isUat, message: "Please fill the url" }]}
              name={["environments", "uat"]}
              style={{ width: "60%" }}
              label="URL:"
            >
              <Input placeholder="Add URL" disabled={!isUat} />
            </Form.Item>
          </Col>
          <Col span={24}>
            <Flex
              className={styles.modalFooter}
              justifyContent="flex-end"
              gap={12}
            >
              <Button onClick={onClose}>Cancel</Button>
              <Button
                type="primary"
                htmlType="submit"
                disabled={isPending}
                loading={isPending}
              >
                OK
              </Button>
            </Flex>
          </Col>
        </Row>
      </Form>
    </div>
  );
};

export default APIEditor;
