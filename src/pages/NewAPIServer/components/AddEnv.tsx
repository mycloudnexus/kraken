import Text from "@/components/Text";
import styles from "./index.module.scss";
import { Col, Form, Row, Input, Checkbox, FormInstance } from "antd";
type Props = {
  form: FormInstance<any>;
  active: boolean;
};
const AddEnv = ({ form, active }: Props) => {
  const name = Form.useWatch("name", form);
  const isSIT = Form.useWatch("isSIT", form);
  const isProd = Form.useWatch("isProd", form);
  const isStage = Form.useWatch("isStage", form);
  const isUat = Form.useWatch("isUat", form);
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
          <Col span={4}>
            <Form.Item name="isSIT" valuePropName="checked">
              <Checkbox>Development</Checkbox>
            </Form.Item>
          </Col>
          <Col span={20}>
            <Form.Item
              name={["environments", "sit"]}
              label="URL:"
              className={styles.inputUrl}
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
              label="URL:"
              className={styles.inputUrl}
              rules={[{ required: isProd, message: "Please fill the url" }]}
            >
              <Input placeholder="Add URL" disabled={!isProd} />
            </Form.Item>
          </Col>
          <Col span={4}>
            <Form.Item name="isStage" valuePropName="checked">
              <Checkbox>Stage</Checkbox>
            </Form.Item>
          </Col>
          <Col span={20}>
            <Form.Item
              name={["environments", "stage"]}
              label="URL:"
              className={styles.inputUrl}
              rules={[{ required: isStage, message: "Please fill the url" }]}
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
              name={["environments", "uat"]}
              label="URL:"
              className={styles.inputUrl}
              rules={[{ required: isUat, message: "Please fill the url" }]}
            >
              <Input placeholder="Add URL" disabled={!isUat} />
            </Form.Item>
          </Col>
        </Row>
      </div>
    </div>
  );
};

export default AddEnv;
