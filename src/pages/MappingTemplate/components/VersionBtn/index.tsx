import Text from "@/components/Text";
import useUser from "@/hooks/user/useUser";
import { IReleaseHistory } from "@/utils/types/product.type";
import {
  CheckCircleFilled,
  CloseCircleFilled,
  InfoCircleOutlined,
} from "@ant-design/icons";
import { Button, Flex, Tooltip } from "antd";
import dayjs from "dayjs";
import { isEmpty } from "lodash";
import { useMemo } from "react";

type Props = {
  item: IReleaseHistory;
};

const VersionBtn = ({ item }: Props) => {
  const { findUserName } = useUser();
  const BtnStage = useMemo(() => {
    if (isEmpty(item.deployments)) {
      return <Button type="default">Deploy to Stage</Button>;
    }
    const currentStage = item.deployments?.find(
      (d) => d.envName?.toUpperCase?.() === "STAGE"
    );
    if (!isEmpty(currentStage)) {
      return (
        <Flex align="center" gap={8}>
          {currentStage?.status?.toUpperCase?.() === "SUCCESS" ? (
            <Flex gap={4} align="center">
              <CheckCircleFilled style={{ color: "#389E0D" }} />
              <Text.LightSmall color="#389E0D">Success</Text.LightSmall>
            </Flex>
          ) : (
            <Flex gap={4} align="center">
              <CloseCircleFilled style={{ color: "#CF1322" }} />
              <Text.LightSmall color="#CF1322">Failed</Text.LightSmall>
            </Flex>
          )}
          <Flex gap={4} align="center">
            <Text.LightMedium lineHeight="22px" color="rgba(0, 0, 0, 0.45)">
              Deployed to Stage
            </Text.LightMedium>
            <Tooltip
              title={`By ${findUserName(currentStage?.upgradeBy)} ${dayjs(
                currentStage.createdAt
              ).format("YYYY-MM-DD HH:mm:ss")}`}
            >
              <InfoCircleOutlined style={{ color: "rgba(0, 0, 0, 0.45)" }} />
            </Tooltip>
          </Flex>
        </Flex>
      );
    }
    return <></>;
  }, [item]);

  const BtnProd = useMemo(() => {
    const currentStage = item.deployments?.find(
      (d) => d.envName?.toUpperCase?.() === "STAGE"
    );
    if (isEmpty(item.deployments)) {
      return (
        <Button type="default" disabled>
          Deploy to Production
        </Button>
      );
    }
    if (!isEmpty(currentStage) && item.deployments?.length > 1) {
      return <Button type="default">Deploy to Production</Button>;
    }
    const currentProd = item.deployments?.find(
      (d) => d.envName?.toUpperCase?.() === "PRODUCTION"
    );
    if (!isEmpty(currentProd)) {
      return (
        <Flex align="center" gap={8}>
          {currentProd?.status?.toUpperCase?.() === "SUCCESS" ? (
            <Flex align="center" gap={4}>
              <CheckCircleFilled style={{ color: "#389E0D" }} />
              <Text.LightSmall color="#389E0D">Success</Text.LightSmall>
            </Flex>
          ) : (
            <Flex align="center" gap={4}>
              <CloseCircleFilled style={{ color: "#CF1322" }} />
              <Text.LightSmall color="#CF1322">Failed</Text.LightSmall>
            </Flex>
          )}
          <Flex align="center" gap={4}>
            <Text.LightMedium lineHeight="22px" color="rgba(0, 0, 0, 0.45)">
              Deployed to Production
            </Text.LightMedium>
            <Tooltip
              title={`By ${findUserName(currentProd?.upgradeBy)} ${dayjs(
                currentProd.createdAt
              ).format("YYYY-MM-DD HH:mm:ss")}`}
            >
              <InfoCircleOutlined style={{ color: "rgba(0, 0, 0, 0.45)" }} />
            </Tooltip>
          </Flex>
        </Flex>
      );
    }
    return <></>;
  }, [item]);
  return (
    <Flex align="center" justify="flex-end" gap={16}>
      {BtnStage}
      {BtnProd}
    </Flex>
  );
};

export default VersionBtn;
