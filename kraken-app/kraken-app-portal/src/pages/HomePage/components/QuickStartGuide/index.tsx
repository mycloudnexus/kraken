import { Text } from "@/components/Text";
import { useGetQuickStartGuide } from "@/hooks/homepage";
import { useAppStore } from "@/stores/app.store";
import { COMPONENT_KIND_API } from "@/utils/constants/product";
import { IQuickStartGuideObject } from "@/utils/types/product.type";
import { Card, Col, Flex, Progress, Row, Spin } from "antd";
import { OneIcon, TwoIcon, ThreeIcon, CheckFilled, CheckGray } from "../Icon";
import styles from "../index.module.scss";

const step = [
  {
    step: "Seller API Server Registration",
    icon: <OneIcon />,
    propKey: "sellerApiServerRegistrationInfo",
    goals: {
      atLeastOneSellerApiRegistered: "Register one seller Proprietary API",
    },
  },
  {
    step: "API Mapping",
    icon: <TwoIcon />,
    propKey: "apiMappingInfo",
    goals: {
      atLeastOneMappingCompleted:
        "Complete mapping for one Sonata API use case.",
    },
  },
  {
    step: "Deployment and Testing",
    icon: <ThreeIcon />,
    propKey: "deploymentInfo",
    goals: {
      atLeastOneApiDeployedToStage:
        "Deploy one Sonata API use case to Stage data plane",
      atLeastOneBuyerRegistered: "Register one buyer",
      atLeastOneApiDeployedToProduction:
        "Release one Stage deployed Sonata API use case to production",
    },
  },
];

const QuickStartGuide = () => {
  const { currentProduct } = useAppStore();
  const { data: quickStartGuideData, isLoading } = useGetQuickStartGuide(
    currentProduct,
    COMPONENT_KIND_API
  );

  const getSteps = (stepKey: keyof IQuickStartGuideObject) => {
    let totalSteps = 0;
    let completedSteps = 0;

    if (quickStartGuideData) {
      const stepInfo = quickStartGuideData[stepKey];
      totalSteps = Object.keys(stepInfo).length;
      completedSteps = Object.values(stepInfo).filter(Boolean).length;
    }
    return { totalSteps, completedSteps };
  };

  // Helper to convert boolean to percentage for Progress component
  const getProgressPercent = (stepKey: keyof IQuickStartGuideObject) => {
    const { completedSteps, totalSteps } = getSteps(stepKey);
    return (completedSteps / totalSteps) * 100;
  };

  return (
    <Flex vertical className={styles.wrapper}>
      <div className={styles.stepContainer}>
        <div className={styles.diagram} />
      </div>
      <Text.NormalLarge style={{ margin: "16px 0 12px 0" }}>
        Quick start guide
      </Text.NormalLarge>
      <Flex className={styles.guideContainer}>
        <Spin spinning={isLoading}>
          <Row gutter={[24, 24]}>
            {quickStartGuideData &&
              step.map((i) => {
                const { completedSteps, totalSteps } = getSteps(
                  i.propKey as keyof IQuickStartGuideObject
                );
                return (
                  <Col key={i.step} lg={8} md={12}>
                    <Card style={{ height: "100%" }}>
                      <Flex gap={9} align="center">
                        {i.icon}
                        <Text.Custom
                          size={16}
                          fontStyle="normal"
                          lineHeight="24px"
                          color="#000"
                        >
                          {i.step}
                        </Text.Custom>
                      </Flex>
                      <Progress
                        percent={getProgressPercent(
                          i.propKey as keyof IQuickStartGuideObject
                        )}
                        showInfo={false}
                        strokeWidth={4}
                        strokeColor={{ "0%": "#2962FF", "100%": "#69D6B3" }}
                      />
                      <Flex style={{ paddingBottom: "15px" }}>
                        {completedSteps} of {totalSteps} completed
                      </Flex>
                      <Flex vertical gap={12}>
                        {Object.keys(i.goals).map((goal) =>
                          quickStartGuideData[
                            i.propKey as keyof IQuickStartGuideObject
                          ][
                            goal as keyof (typeof quickStartGuideData)[keyof IQuickStartGuideObject]
                          ] ? (
                            <Flex key={goal} gap={10}>
                              <div className={styles.iconContainer}>
                                <CheckFilled />
                              </div>
                              <Text.LightMedium>
                                {i.goals[goal as keyof typeof i.goals]}
                              </Text.LightMedium>
                            </Flex>
                          ) : (
                            <Flex key={goal}>
                              <CheckGray />
                              <Text.LightMedium
                                style={{ color: "rgba(0, 0, 0, 0.45)" }}
                              >
                                {i.goals[goal as keyof typeof i.goals]}
                              </Text.LightMedium>
                            </Flex>
                          )
                        )}
                      </Flex>
                    </Card>
                  </Col>
                );
              })}
          </Row>
        </Spin>
      </Flex>
    </Flex>
  );
};

export default QuickStartGuide;
