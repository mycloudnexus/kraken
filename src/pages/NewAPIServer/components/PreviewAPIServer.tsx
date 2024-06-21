import { Button, Col, Form, FormInstance, Row } from "antd";
import styles from "./index.module.scss";
import Text from "@/components/Text";
import TitleIcon from "@/assets/title-icon.svg";
import Flex from "@/components/Flex";
import RequestMethod from "@/components/Method";
import { get, isEmpty } from "lodash";
import { PaperClipOutlined } from "@ant-design/icons";

type Props = {
  form: FormInstance<any>;
  active: boolean;
  handleBack: (step: number) => void;
};

const PreviewAPIServer = ({ form, active, handleBack }: Props) => {
  const selectedAPIs = Form.useWatch("selectedAPIs", form);
  const sit = Form.useWatch(["environments", "sit"], form);
  const prod = Form.useWatch(["environments", "prod"], form);
  const stage = Form.useWatch(["environments", "stage"], form);
  const uat = Form.useWatch(["environments", "uat"], form);
  const name = Form.useWatch("name", form);
  const link = Form.useWatch("link", form);
  const file = Form.useWatch("file", form);
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
          boxSizing: 'border-box'
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
          <Row gutter={[12, 12]}>
            <Col span={8}>
              <Flex flexDirection="column" alignItems="flex-start">
                <Text.LightMedium color="#00000073">
                  Seller API Server Name
                </Text.LightMedium>
                <Text.LightMedium>{name}</Text.LightMedium>
              </Flex>
            </Col>
            <Col span={16}>
              <Flex flexDirection="column" alignItems="flex-start">
                <Text.LightMedium color="#00000073">
                  Online API document link
                </Text.LightMedium>
                <Text.LightMedium>{link}</Text.LightMedium>
              </Flex>
            </Col>
            <Col span={24}>
              <Flex flexDirection="column" alignItems="flex-start">
                <Text.LightMedium color="#00000073">
                  Description
                </Text.LightMedium>
                <Text.LightMedium>{link}</Text.LightMedium>
              </Flex>
            </Col>
            <Col span={24}>
              <Flex flexDirection="column" alignItems="flex-start">
                <Text.LightMedium color="#000000D9">
                  API spec in yaml format
                </Text.LightMedium>
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
          {!isEmpty(sit) && (
            <Flex gap={8} justifyContent="flex-start">
              <Text.LightMedium style={{ width: 120 }}>
                Development
              </Text.LightMedium>
              <Text.LightMedium>{sit}</Text.LightMedium>
            </Flex>
          )}
          {!isEmpty(prod) && (
            <Flex gap={8} justifyContent="flex-start">
              <Text.LightMedium style={{ width: 120 }}>
                Production
              </Text.LightMedium>
              <Text.LightMedium>{prod}</Text.LightMedium>
            </Flex>
          )}
          {!isEmpty(stage) && (
            <Flex gap={8} justifyContent="flex-start">
              <Text.LightMedium style={{ width: 120 }}>Stage</Text.LightMedium>
              <Text.LightMedium>{stage}</Text.LightMedium>
            </Flex>
          )}
          {!isEmpty(uat) && (
            <Flex gap={8} justifyContent="flex-start">
              <Text.LightMedium style={{ width: 120 }}>UAT</Text.LightMedium>
              <Text.LightMedium>{uat}</Text.LightMedium>
            </Flex>
          )}
        </Flex>
      </div>
    </div>
  );
};

export default PreviewAPIServer;
