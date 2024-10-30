import styles from "./index.module.scss";
import { Button, Col, Flex, Row } from "antd";

import { Step1Icon, RightArrow, Step2Icon, Step3Icon, Step4Icon } from "./Icon";
import { Fragment } from "react/jsx-runtime";
import { Text } from "@/components/Text";
import { useBoolean } from "usehooks-ts";
const step = [
  {
    step: "Step 1",
    title: "Get to know supported Sonata Functionality Matrix",
    icon: <Step1Icon />,
    desc: "Our current offering includes a focused set of business functionalities centered around supporting buyer to order UNI and Access E-line successfully from seller which covers 3 components : Address Validation, Quote Management and Order Management",
  },
  {
    step: "Step 2",
    title: "Investigate to know the seller Legacy API Platform",
    icon: <Step2Icon />,
    desc: "Since MEF LSO Sonata Adapter is built on top of Seller’s legacy API platform, you need to first get some basic information about it like open api spec, online documentation, sandbox url and account etc firstly and understand the API flow to fulfill Sonata Use Case.",
  },
  {
    step: "Step 3",
    title: "Register seller API Server in Kraken",
    icon: <Step3Icon />,
    desc: "Register seller API server by uploading all the information gathered in step 2 so that MEF LSO Sonata Adapter can trigger it in sonata API request by buyer side.",
  },
  {
    step: "Step 4",
    title: "Map standard APIs with seller APIs",
    icon: <Step4Icon />,
    desc: "Map each combination of (standard API, API action type, product type) by selecting one Seller API from Seller API Server and Complete Request Mapping and Response Mapping based on Kraken predefined mapping template.",
  },
];

const StepCard = (props: {
  navigateApi: () => void;
  navigateCreateAPI: () => void;
}) => {
  const { navigateApi, navigateCreateAPI } = props;
  const { value: isShow, setFalse: hidden } = useBoolean(true);
  if (!isShow) {
    return <div />;
  }
  return (
    <div className={styles.stepContainer}>
      <Row justify={"space-between"}>
        <h3>How to get started with Kraken?</h3>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          onClick={hidden}
          style={{ cursor: "pointer" }}
        >
          <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M12 4C7.58172 4 4 7.58172 4 12C4 16.4183 7.58172 20 12 20C16.4183 20 20 16.4183 20 12C20 7.58172 16.4183 4 12 4ZM2 12C2 6.47715 6.47715 2 12 2C17.5228 2 22 6.47715 22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12ZM10.5892 12.0034L8.11433 9.52856L9.52854 8.11435L12.0034 10.5892L14.4783 8.11435L15.8925 9.52856L13.4176 12.0034L15.8925 14.4783L14.4783 15.8925L12.0034 13.4176L9.52854 15.8925L8.11433 14.4783L10.5892 12.0034Z"
            fill="white"
          />
        </svg>
      </Row>
      <Row
        className={styles.stepRow}
        align={"middle"}
        justify={"space-between"}
      >
        {step.map((i, n) => {
          return (
            <Fragment key={i.step}>
              <Col span={5} className={styles.stepDetail}>
                <Flex vertical gap={9} align="center">
                  <Text.Custom
                    bold="500"
                    size="20px"
                    lineHeight="28px"
                    className={styles.stepCount}
                    color="#fff"
                  >
                    {i.step}
                  </Text.Custom>
                  <span
                    className={styles.title}
                    style={{
                      color:
                        n == 2
                          ? "var(--Text-Text_accent, #2962FF)"
                          : "var(--Text-primary, rgba(0, 0, 0, 0.85)",
                      cursor: "pointer",
                    }}
                    onClick={n == 2 ? navigateApi : undefined}
                  >
                    {i.title}
                  </span>
                </Flex>
                <div className={styles.iconContainer}> {i.icon}</div>
                <p>{i.desc}</p>
              </Col>
              {n !== 3 && (
                <Col
                  span={1}
                  className={styles.arrow}
                  style={{ textAlign: "center" }}
                >
                  <RightArrow />
                </Col>
              )}
            </Fragment>
          );
        })}
      </Row>
      <Flex
        justify="flex-end"
        align="center"
        gap={12}
        style={{ marginTop: 20 }}
        onClick={navigateCreateAPI}
      >
        <Text.LightLarge color="#fff">
          Start register seller API server now
        </Text.LightLarge>
        <Button type="primary">Create API server</Button>
      </Flex>
    </div>
  );
};

export default StepCard;
