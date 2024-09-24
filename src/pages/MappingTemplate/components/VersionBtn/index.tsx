import Text from "@/components/Text";
import {
  useDeployMappingTemplateProduction,
  useDeployMappingTemplateStage,
  useGetProductEnvs,
} from "@/hooks/product";
import useUser from "@/hooks/user/useUser";
import { useAppStore } from "@/stores/app.store";
import { IReleaseHistory } from "@/utils/types/product.type";
import {
  CheckCircleFilled,
  CloseCircleFilled,
  InfoCircleOutlined,
  RedoOutlined,
} from "@ant-design/icons";
import { Button, Flex, Modal, Tooltip, notification } from "antd";
import dayjs from "dayjs";
import { get, isEmpty } from "lodash";
import { useMemo } from "react";

type Props = {
  item: IReleaseHistory;
};

const VersionBtn = ({ item }: Props) => {
  const { currentProduct } = useAppStore();
  const { findUserName } = useUser();
  const { mutateAsync: deployStage, isPending: pendingStage } =
    useDeployMappingTemplateStage();
  const { mutateAsync: deployProd, isPending: pendingProd } =
    useDeployMappingTemplateProduction();
  const { data: dataEnv } = useGetProductEnvs(currentProduct);
  const stageEnv = dataEnv?.data?.find(
    (d) => d?.name?.toLowerCase?.() === "stage"
  );
  const productionEnv = dataEnv?.data?.find(
    (d) => d?.name?.toLowerCase?.() === "production"
  );

  const handleUpgradeStage = async () => {
    Modal.confirm({
      icon: <></>,
      title: `Are you sure to upgrade now?`,
      content: (
        <Text.NormalMedium>
          Upgrade may take a few minutes??. You will not be able to change the
          API mapping configurations or perform new deployment during the
          process. Continue?
        </Text.NormalMedium>
      ),

      okText: "Upgrade",
      okType: "primary",
      closable: false,
      onOk: async () => {
        try {
          const res = await deployStage({
            productId: currentProduct,
            data: {
              templateUpgradeId: item.templateUpgradeId,
              stageEnvId: stageEnv?.id,
            },
          } as any);
          if (res) {
            notification.success({ message: get(res, "message", "Success!") });
          }
        } catch (error) {
          notification.error({
            message: get(error, "reason", "Error. Please try again"),
          });
        }
      },
    });
  };

  const handleUpgradeProd = async () => {
    Modal.confirm({
      icon: <></>,
      title: `Are you sure to upgrade now?`,
      content: (
        <Text.NormalMedium>
          Upgrade may take a few minutes??. You will not be able to change the
          API mapping configurations or perform new deployment during the
          process. Continue?
        </Text.NormalMedium>
      ),

      okText: "Upgrade",
      okType: "primary",
      closable: false,
      onOk: async () => {
        try {
          const res = await deployProd({
            productId: currentProduct,
            data: {
              templateUpgradeId: item.templateUpgradeId,
              stageEnvId: stageEnv?.id,
              productEnvId: productionEnv?.id,
            },
          } as any);
          if (res) {
            notification.success({ message: get(res, "message", "Success!") });
          }
        } catch (error) {
          notification.error({
            message: get(error, "reason", "Error. Please try again"),
          });
        }
      },
    });
  };

  const BtnStage = useMemo(() => {
    if (item.showStageUpgradeButton && isEmpty(item.deployments)) {
      return (
        <Button
          type="default"
          onClick={handleUpgradeStage}
          loading={pendingStage}
        >
          {pendingStage ? "Deploying" : "Deploy to Stage"}
        </Button>
      );
    }
    const currentStage = item.deployments?.find(
      (d) => d.envName?.toUpperCase?.() === "STAGE"
    );
    if (!isEmpty(currentStage)) {
      if (currentStage.status === "IN_PROCESS" || !currentStage.status) {
        return (
          <Button type="default" icon={<RedoOutlined rotate={-90} />}>
            Deploying
          </Button>
        );
      }
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
  }, [item, pendingStage, handleUpgradeStage]);

  const BtnProd = useMemo(() => {
    const currentStage = item.deployments?.find(
      (d) => d.envName?.toUpperCase?.() === "STAGE"
    );
    if (
      (isEmpty(item.deployments) && item.showStageUpgradeButton) ||
      (currentStage &&
        item.deployments?.length === 1 &&
        !item.showProductionUpgradeButton)
    ) {
      return (
        <Button type="default" disabled>
          Deploy to Production
        </Button>
      );
    }
    if (
      !isEmpty(currentStage) &&
      item.deployments?.length === 1 &&
      item.showProductionUpgradeButton
    ) {
      return (
        <Button
          type="default"
          onClick={handleUpgradeProd}
          loading={pendingProd}
        >
          {pendingProd ? "Deploying" : "Deploy to Production"}
        </Button>
      );
    }
    const currentProd = item.deployments?.find(
      (d) => d.envName?.toUpperCase?.() === "PRODUCTION"
    );
    if (!isEmpty(currentProd)) {
      if (currentProd.status === "IN_PROCESS" || !currentProd.status) {
        return (
          <Button type="default" icon={<RedoOutlined rotate={-90} />}>
            Deploying
          </Button>
        );
      }
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
  }, [item, pendingProd, handleUpgradeProd]);

  return (
    <Flex align="center" justify="flex-end" gap={16} style={{ minHeight: 32 }}>
      {isEmpty(item.deployments) && !item.showStageUpgradeButton ? (
        <Flex gap={4} align="center">
          <InfoCircleOutlined style={{ color: "#FF9A2E" }} />
          <Text.LightMedium color="#FF9A2E" lineHeight="22px">
            Deprecated
          </Text.LightMedium>
        </Flex>
      ) : (
        <>
          {BtnStage}
          {BtnProd}
        </>
      )}
    </Flex>
  );
};

export default VersionBtn;
