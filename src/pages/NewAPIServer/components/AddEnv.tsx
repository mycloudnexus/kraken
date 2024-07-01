import Text from "@/components/Text";
import styles from "./index.module.scss";
import { Col, Form, Row, Input, Checkbox, FormInstance } from "antd";
import { IEnv } from "@/utils/types/env.type";
import Flex from "@/components/Flex";
import TitleIcon from "@/assets/title-icon.svg";
import { isURL } from "@/utils/helpers/url";
import { isEmpty } from "lodash";
import { Fragment } from "react";

type Props = {
  form: FormInstance<any>;
  env: IEnv[];
};
const AddEnv = ({ form, env }: Props) => {
  return (
    <Row gutter={[40, 16]}>
      <Col span={24}>
        <Flex gap={8} justifyContent="flex-start">
          <TitleIcon />
          <Text.NormalLarge>
            Base URL for Environments(at least one){" "}
          </Text.NormalLarge>
          <span style={{ color: "#FF4D4F" }}>*</span>
        </Flex>
      </Col>
      {env?.map((e) => (
        <Fragment key={e.id}>
          <Col span={4}>
            <Form.Item name={`is${e.name}`} valuePropName="checked">
              <Checkbox
                onChange={(event) => {
                  if (!event.target.checked) {
                    form.setFieldValue(["environments", e.name], undefined);
                  }
                }}
              >
                {e.name}
              </Checkbox>
            </Form.Item>
          </Col>
          <Col span={20}>
            <Form.Item noStyle shouldUpdate>
              {({ getFieldValue }) => {
                const isEnv = getFieldValue(`is${e.name}`);
                return (
                  <Form.Item
                    name={["environments", e.name]}
                    label="URL:"
                    className={styles.inputUrl}
                    rules={[
                      { required: isEnv, message: "Please fill the url" },
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
                  >
                    <Input placeholder="Add URL" disabled={!isEnv} />
                  </Form.Item>
                );
              }}
            </Form.Item>
          </Col>
        </Fragment>
      ))}
    </Row>
  );
};

export default AddEnv;
