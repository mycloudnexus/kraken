import {
  Button,
  Checkbox,
  Col,
  Form,
  FormInstance,
  Input,
  Row,
  Upload,
  notification,
} from "antd";
import Text from "../Text";
import { PaperClipOutlined, UploadOutlined } from "@ant-design/icons";
import Flex from "../Flex";
import {
  cloneDeep,
  get,
  isEmpty,
  isNull,
  isUndefined,
  pickBy,
  set,
} from "lodash";
import { isURL } from "@/utils/helpers/url";
import { Fragment, useEffect, useState } from "react";
import { decode } from "js-base64";
import jsYaml from "js-yaml";
import { useAppStore } from "@/stores/app.store";
import TitleIcon from "@/assets/title-icon.svg";
import { useNavigate } from "react-router-dom";
import RequestMethod from "../Method";
import { useGetProductEnvs } from "@/hooks/product";
import styles from "./index.module.scss";

type Props = {
  detail: any;
  onClose?: () => void;
  refresh?: () => void;
  componentId: string;
  form: FormInstance<any>;
  runUpdate: any;
};

const APIEditor = ({
  detail,
  onClose,
  refresh,
  componentId,
  form,
  runUpdate,
}: Props) => {
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();
  const { data: dataEnv } = useGetProductEnvs(currentProduct);
  const env = get(dataEnv, "data", []);
  const [isChangedFile, setIsChangedFile] = useState(false);
  const file = Form.useWatch("file", form);
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
      const environments = get(detail, "facets.environments");
      let newEnv = {};
      const keys = Object.keys(environments);

      for (const key of keys) {
        if (!isEmpty(environments[key])) {
          set(newEnv, `is${key}`, true);
        }
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
                `${get(swaggerData, "info.title", "file")}.yaml`,
                {
                  type: "application/x-yaml",
                }
              ),
        },
        environments,
        ...newEnv,
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
        message: get(
          error,
          "reason",
          get(error, "message", "Error. Please try again")
        ),
      });
    }
  };

  return (
    <div>
      <Form form={form} onFinish={onFinish}>
        <Row gutter={[24, 24]} style={{ width: "100%" }}>
          <Col span={24}>
            <Flex gap={8} justifyContent="flex-start">
              <TitleIcon />
              <Text.NormalLarge>Seller API Server basics</Text.NormalLarge>
            </Flex>
            <Row gutter={[32, 6]} style={{ marginTop: 12 }}>
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
                  <Input
                    placeholder="Add description"
                    style={{ width: "100%" }}
                  />
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
            </Row>
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
            <Flex gap={8} justifyContent="flex-start">
              <TitleIcon />
              <Text.NormalLarge>Seller API</Text.NormalLarge>
              <Button
                type="text"
                style={{ color: "#2962FF" }}
                onClick={() =>
                  navigate(
                    `/component/${currentProduct}/edit/${get(
                      detail,
                      "metadata.key"
                    )}/api`
                  )
                }
              >
                Edit
              </Button>
            </Flex>
            <div style={{ marginTop: 12 }}>
              <Flex flexDirection="column" gap={8} alignItems="flex-start">
                {get(detail, "facets.selectedAPIs")?.map((api: string) => (
                  <Flex key={api} gap={8} justifyContent="flex-start">
                    <div style={{ width: 58 }}>
                      <RequestMethod method={get(api.split(" "), "[1]")} />
                    </div>
                    <Text.LightMedium>
                      {get(api.split(" "), "[0]")}
                    </Text.LightMedium>
                  </Flex>
                ))}
              </Flex>
            </div>
          </Col>
          <Col span={24}>
            <Flex
              gap={12}
              flexDirection="column"
              alignItems="flex-start"
              justifyContent="flex-start"
            >
              <Flex gap={8} justifyContent="flex-start">
                <TitleIcon />
                <Text.NormalLarge>
                  Base URL for environment variables
                </Text.NormalLarge>
              </Flex>
              <Row gutter={[8, 8]}>
                <Form.Item
                  noStyle
                  shouldUpdate
                  name="environments"
                  rules={[
                    () => ({
                      validator(_, value) {
                        const currentValue = pickBy(
                          value,
                          (value) =>
                            !isNull(value) &&
                            value !== "" &&
                            !isUndefined(value)
                        );
                        const isValue = !isEmpty(currentValue);

                        if (isValue) {
                          return Promise.resolve();
                        }
                        return Promise.reject(
                          new Error("Please fill at least one environment")
                        );
                      },
                    }),
                  ]}
                >
                  {env?.map((e) => (
                    <Fragment key={e?.name}>
                      <Col span={4}>
                        <Form.Item name={`is${e.name}`} valuePropName="checked">
                          <Checkbox
                            onChange={(event) => {
                              if (!event?.target?.checked) {
                                form.setFieldValue(
                                  ["environments", e.name],
                                  undefined
                                );
                              }
                              form.setFieldValue(
                                "is" + e.name,
                                event.target.checked
                              );
                            }}
                          >
                            {e.name}
                          </Checkbox>
                        </Form.Item>
                      </Col>
                      <Col span={20}>
                        <Form.Item noStyle shouldUpdate>
                          {({ getFieldValue }) => {
                            const isEnv = Boolean(getFieldValue(`is${e.name}`));
                            return (
                              <Form.Item
                                name={["environments", e.name]}
                                label="URL:"
                                className={styles.inputUrl}
                                rules={[
                                  {
                                    required: isEnv,
                                    message: "Please fill the url",
                                  },
                                ]}
                              >
                                <Input
                                  placeholder="Add URL"
                                  disabled={!isEnv}
                                />
                              </Form.Item>
                            );
                          }}
                        </Form.Item>
                      </Col>
                    </Fragment>
                  ))}
                  <Form.Item noStyle shouldUpdate>
                    {({ getFieldError }) => {
                      const error = getFieldError("environments");
                      return isEmpty(error) ? null : (
                        <Text.LightSmall color="red">
                          Please fill at least one environment
                        </Text.LightSmall>
                      );
                    }}
                  </Form.Item>
                </Form.Item>
              </Row>
            </Flex>
          </Col>
        </Row>
      </Form>
    </div>
  );
};

export default APIEditor;
