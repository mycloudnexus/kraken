import Text from "@/components/Text";
import styles from "./index.module.scss";
import { Col, Form, Row, Input, Checkbox, FormInstance } from "antd";
import { IEnv } from "@/utils/types/env.type";
type Props = {
  form: FormInstance<any>;
  active: boolean;
  env: IEnv[];
};
const AddEnv = ({ form, active, env }: Props) => {
  const name = Form.useWatch("name", form);

  return (
    <div
      style={{
        display: active ? "flex" : "none",
        flex: 1,
        flexDirection: "column",
      }}
    >
      <Text.BoldLarge>Add base URL for environment variables</Text.BoldLarge>
      <div className={styles.paper} style={{ flex: 1 }}>
        <Row gutter={[40, 16]}>
          <Col span={24}>
            <Text.NormalLarge>{name}</Text.NormalLarge>
          </Col>
          <Col span={24}>
            <Text.NormalMedium>
              Base URL for environment variables{" "}
              <span style={{ color: "#FF4D4F" }}>*</span>
            </Text.NormalMedium>
          </Col>
          {env?.map((e) => (
            <>
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
                        ]}
                      >
                        <Input placeholder="Add URL" disabled={!isEnv} />
                      </Form.Item>
                    );
                  }}
                </Form.Item>
              </Col>
            </>
          ))}
        </Row>
      </div>
    </div>
  );
};

export default AddEnv;
