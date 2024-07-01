import { Button, Col, Form, FormInstance, Row } from "antd";
import styles from "./index.module.scss";
import Text from "@/components/Text";
import TitleIcon from "@/assets/title-icon.svg";
import Flex from "@/components/Flex";
import RequestMethod from "@/components/Method";
import { get } from "lodash";
import { PaperClipOutlined } from "@ant-design/icons";
import { IEnv } from "@/utils/types/env.type";

type Props = {
  form: FormInstance<any>;
  active: boolean;
  handleBack: (step: number) => void;
  env: IEnv[];
};

const PreviewAPIServer = ({ form, active, handleBack, env }: Props) => {
  const selectedAPIs = Form.useWatch("selectedAPIs", form);
  const name = Form.useWatch("name", form);
  const link = Form.useWatch("link", form);
  const file = Form.useWatch("file", form);
  const description = Form.useWatch("description", form);
  return (
    <div
      style={{
        display: active ? "flex" : "none",
        flex: 1,
        flexDirection: "column",
        overflowY: "auto",
      }}
    >
      <Text.BoldLarge>Review</Text.BoldLarge>
      <div
        className={styles.paper}
        style={{
          flex: 1,
          display: "flex",
          flexDirection: "column",
          gap: 12,
          width: "100%",
          boxSizing: "border-box",
        }}
      >
        <Flex gap={8} justifyContent="flex-start">
          <TitleIcon />
          <Text.NormalLarge>Seller API Server basics</Text.NormalLarge>
          <Button
            type="text"
            style={{ color: "#2962FF" }}
            onClick={() => handleBack(0)}
          >
            Edit
          </Button>
        </Flex>
        <div>
          <Row gutter={[20, 20]}>
            <Col span={8}>
              <Flex flexDirection="column" alignItems="flex-start" gap={8}>
                <Text.LightMedium color="#00000073">
                  Seller API Server Name
                </Text.LightMedium>
                <Text.LightMedium>{name}</Text.LightMedium>
              </Flex>
            </Col>
            <Col span={16}>
              <Flex flexDirection="column" alignItems="flex-start" gap={8}>
                <Text.LightMedium color="#00000073">
                  Online API document link
                </Text.LightMedium>
                <Text.LightMedium>{link ?? "-"}</Text.LightMedium>
              </Flex>
            </Col>
            <Col span={24}>
              <Flex flexDirection="column" alignItems="flex-start" gap={8}>
                <Text.LightMedium color="#00000073">
                  Description
                </Text.LightMedium>
                <Text.LightMedium>{description ?? "-"}</Text.LightMedium>
              </Flex>
            </Col>
            <Col span={24}>
              <Flex flexDirection="column" alignItems="flex-start" gap={4}>
                <Text.NormalMedium color="#000000D9">
                  API spec in yaml format
                </Text.NormalMedium>
                <Flex gap={9} justifyContent="flex-start">
                  <PaperClipOutlined />
                  <Text.LightMedium>
                    {get(file, "file.name", "")}
                  </Text.LightMedium>
                </Flex>
              </Flex>
            </Col>
          </Row>
        </div>
        <Flex gap={8} justifyContent="flex-start">
          <TitleIcon />
          <Text.NormalLarge>Seller API</Text.NormalLarge>
          <Button
            type="text"
            style={{ color: "#2962FF" }}
            onClick={() => handleBack(1)}
          >
            Edit
          </Button>
        </Flex>
        <Flex flexDirection="column" gap={8} alignItems="flex-start">
          {selectedAPIs?.map((api: string) => (
            <Flex key={api} gap={8} justifyContent="flex-start">
              <RequestMethod method={get(api.split(" "), "[1]")} />
              <Text.LightMedium>{get(api.split(" "), "[0]")}</Text.LightMedium>
            </Flex>
          ))}
        </Flex>
        <Flex gap={8} justifyContent="flex-start">
          <TitleIcon />
          <Text.NormalLarge>
            Base URL for environment variables
          </Text.NormalLarge>
          <Button
            type="text"
            style={{ color: "#2962FF" }}
            onClick={() => handleBack(2)}
          >
            Edit
          </Button>
        </Flex>
        <Flex flexDirection="column" gap={8} alignItems="flex-start">
          {env?.map((e) =>
            form.getFieldValue(["environments", e.name]) ? (
              <Flex gap={8} justifyContent="flex-start" key={e.name}>
                <Text.LightMedium style={{ width: 120 }}>
                  {e.name}
                </Text.LightMedium>
                <Text.LightMedium>
                  URL: {form.getFieldValue(["environments", e.name])}
                </Text.LightMedium>
              </Flex>
            ) : null
          )}
        </Flex>
      </div>
    </div>
  );
};

export default PreviewAPIServer;
