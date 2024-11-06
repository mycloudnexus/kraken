import { Alert } from "@/components/Alert";
import BreadCrumb from "@/components/Breadcrumb";
import { PageLayout } from "@/components/Layout";
import { Steps } from "@/components/Steps";
import { useEnv } from "@/hooks/env";
import {
  useControlPlaneTemplateUpgrade,
  useGetTemplateMappingReleaseDetail,
  useProductionTemplateUpgrade,
  useStageTemplateUpgrade,
  useProductionUpgradeCheck,
  useStageUpgradeCheck,
} from "@/hooks/mappingTemplate";
import { useLongPolling } from "@/hooks/useLongPolling";
import { useAppStore } from "@/stores/app.store";
import { Deployment } from "@/utils/types/product.type";
import { ReloadOutlined } from "@ant-design/icons";
import { Button, Flex, Spin, StepsProps, Tag } from "antd";
import classNames from "classnames";
import { nanoid } from "nanoid";
import { lazy, ReactNode, Suspense, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useMappingTemplateStoreV2 } from "../store";
import { getUpgradeSteps, LONG_POLLING_TIME } from "../utils";
import { DeprecatedModal } from "./components/DeprecatedModal";
import { IncompatibleMappingModal } from "./components/IncompatibleMappingModal";
import { StartUpgradeModal } from "./components/StartUpgradeModal";
import styles from "./index.module.scss";

const ControlPlaneUpgrade = lazy(() => import("./ControlPlaneUpgrade"));
const StageUpgrade = lazy(() => import("./StageUpgrade"));
const ProductionUpgrade = lazy(() => import("./ProductionUpgrade"));

function getStepStatus(
  step: number,
  currentStep: number
): StepsProps["status"] {
  if (currentStep > step) return "finish";
  if (currentStep === step) return "process";
  return "wait";
}

function getUpgradeButtonText(
  isUpgrading: boolean,
  isUpgraded: boolean,
  currentStep: number
): ReactNode {
  if (isUpgrading)
    return (
      <>
        <ReloadOutlined /> Upgrading
      </>
    );

  if (isUpgraded) {
    if (currentStep === 2) return 'Done'
    return "Next";
  }

  return "Upgrade now";
}

export default function UpgradePlane() {
  const { templateUpgradeId = "" } = useParams<{ templateUpgradeId: string }>();
  const navigate = useNavigate();

  const { currentProduct: productId } = useAppStore();
  const { findEnvByName } = useEnv(productId);

  const [shouldRefetchTemplateDetail, setShouldRefetchTemplateDetail] = useState(false)
  // Fetch release detail and check for upgrade statuses
  const {
    data: templateDetail,
    isLoading: isLoadingTemplateDetail,
    refetch: refetchTemplateDetail,
  } = useLongPolling(
    useGetTemplateMappingReleaseDetail(productId, templateUpgradeId),
    LONG_POLLING_TIME,
    { active: shouldRefetchTemplateDetail }
  );

  const {
    isMappingIncomplete,
    confirmUpgrade,
    notification,
    setConfirmUpgrade,
    pushNotification,
    removeNotification,
    clearNotification,
    reset,
  } = useMappingTemplateStoreV2();

  const onUpgradeFinished = () => ({
    onSuccess(message: string) {
      pushNotification({ id: nanoid(), message, type: "success" });
    },
    onError(message: string) {
      pushNotification({ id: nanoid(), message, type: "warning" });
    },
  });

  // @TODO: Available environment name for now: stage | production
  const stageEnvId = useMemo(() => findEnvByName("stage")?.id, [findEnvByName]);
  const productEnvId = useMemo(
    () => findEnvByName("production")?.id,
    [findEnvByName]
  );

  // Pre-condition check for upgrade compatibility
  const {
    data: stageUpgradeCheckResult,
    isFetching: isCheckingStageUpgrade,
    refetch: checkStageUpgrade,
  } = useStageUpgradeCheck(productId, templateUpgradeId, stageEnvId as any, { enabled: false });

  const {
    data: productionUpgradeCheckResult,
    isFetching: isCheckingProductionUpgrade,
    refetch: checkProductionUpgrade,
  } = useProductionUpgradeCheck(productId, templateUpgradeId, productEnvId as any, { enabled: false });

  // Upgrade actions
  const {
    mutateAsync: controlPlaneUpgrade,
    isPending: isPendingControlPlaneUpgrade,
  } = useControlPlaneTemplateUpgrade(templateUpgradeId, onUpgradeFinished());

  const { mutateAsync: stageUpgrade, isPending: isPendingStageUpgrade } =
    useStageTemplateUpgrade(templateUpgradeId, onUpgradeFinished());

  const {
    mutateAsync: productionUpgrade,
    isPending: isPendingProductionUpgrade,
  } = useProductionTemplateUpgrade(templateUpgradeId, onUpgradeFinished());

  // State
  const [currentStep, setCurrentStep] = useState(-1);
  const [currentUpgrade, setCurrentUpgrade] = useState<Deployment | undefined>(
    undefined
  );
  const [isTemplateDeprecated, setIsTemplateDeprecated] = useState(false);

  const handleUpgrade = async () => {
    // Check for deprecated template mapping
    if (templateDetail?.status === "Deprecated") {
      setIsTemplateDeprecated(true);
      return;
    }

    switch (currentStep) {
      case 0:
        await controlPlaneUpgrade({ templateUpgradeId });
        refetchTemplateDetail()
        break;

      case 1:
        await checkStageUpgrade()
        if (stageUpgradeCheckResult?.length) {
          pushNotification(...stageUpgradeCheckResult.map(msg => ({
            id: nanoid(),
            type: 'warning',
            message: msg
          })) as any)
          return
        }

        await stageUpgrade({
          templateUpgradeId,
          stageEnvId: stageEnvId as any,
        });
        refetchTemplateDetail()
        break;

      case 2:
        await checkProductionUpgrade()
        if (productionUpgradeCheckResult?.length) {
          pushNotification(...productionUpgradeCheckResult.map(msg => ({
            id: nanoid(),
            type: 'warning',
            message: msg
          })) as any)
          return
        }

        await productionUpgrade({
          templateUpgradeId,
          stageEnvId: stageEnvId as any,
          productEnvId: productEnvId as any,
        });
        refetchTemplateDetail()
        break;

      default:
        break;
    }
  };

  const handleCancel = () => navigate("/mapping-template-v2");

  const startUpgrade = () => {
    if (isUpgraded) {
      if (currentStep < 2) {
        setCurrentStep(currentStep + 1);

        // Clear notification
        clearNotification();
      } else {
        handleCancel();
      }
    } else {
      setConfirmUpgrade(true);
    }
  };

  useEffect(() => {
    if (templateDetail) {
      // Prevent processing if template is deprecated
      if (templateDetail.status === "Deprecated") {
        handleCancel();
        return;
      }

      // Determine step
      const steps = getUpgradeSteps(templateDetail.deployments);

      for (let i = 0; i < steps.length; i++) {
        // Either select first step whose status is not SUCCESS or select the last step
        // It's assured that the upgrade process is stopped at some step
        if (steps[i].status !== "SUCCESS" || i === steps.length - 1) {
          if (currentStep === -1) {
            setCurrentStep(i);
          }

          setCurrentUpgrade(steps[i]);
          break;
        }
      }
    }
  }, [templateDetail]);

  const isSendingUpgrade =
    isPendingControlPlaneUpgrade ||
    isPendingStageUpgrade ||
    isPendingProductionUpgrade;

  const isUpgrading = currentUpgrade?.status === "IN_PROGRESS";
  const isUpgraded = currentUpgrade?.status === "SUCCESS";

  useEffect(() => {
    setShouldRefetchTemplateDetail(isUpgrading)
  }, [isUpgrading, setShouldRefetchTemplateDetail])

  useEffect(() => {
    if (isUpgraded) {
      clearNotification();
 
      switch (currentStep) {
        case 0:
          pushNotification({
            id: nanoid(),
            type: "success",
            message: "Control plane upgrade successfully",
          });
          break;
        case 1:
          pushNotification({
            id: nanoid(),
            type: "success",
            message:
              "Mapping template upgrade successfully and effective now in stage data plane. Please test offline and ensure they can work properly.",
          });
          break;
        case 2:
          pushNotification({
            id: nanoid(),
            type: "success",
            message:
              "Mapping template upgrade successfully and effective now in production data plane. ",
          });
          break;
        default:
          break;
      }
    }
  }, [currentStep, isUpgraded]);

  useEffect(() => {
    reset();
  }, []);

  return (
    <PageLayout
      title={
        <BreadCrumb
          lastItem={
            <span style={{ display: "flex", alignItems: "center", gap: 4 }}>
              {templateDetail?.productVersion}{" "}
              <Tag>{templateDetail?.productSpec}</Tag>
            </span>
          }
          mainTitle="Mapping template release & Upgrade v2"
          mainUrl={`/mapping-template-v2`}
        />
      }
      flex
      vertical
    >
      <Flex className={classNames(styles.card, styles.stepsNav)}>
        <Steps
          type="navigation"
          current={currentStep}
          className="site-navigation-steps"
          items={[
            {
              title: "Control plane upgrade",
              status: currentStep > 0 ? "finish" : "process",
              className: isUpgrading || isPendingControlPlaneUpgrade ? "upgrading" : "",
            },
            {
              title: "Data plane upgrade: Stage",
              status: getStepStatus(1, currentStep),
              className: isUpgrading || isPendingStageUpgrade ? "upgrading" : "",
            },
            {
              title: "Data plane upgrade: Production",
              status: getStepStatus(2, currentStep),
              className: isUpgrading || isPendingProductionUpgrade ? "upgrading" : "",
            },
          ]}
        />
      </Flex>

      <Flex className={classNames(styles.card, styles.flexOne)}>
        <Spin spinning={isLoadingTemplateDetail}>
          <Flex vertical gap={8} style={{ height: "100%" }}>
            {notification.map((noti) => (
              <Alert
                key={noti.id}
                className={styles.notiBadge}
                showIcon
                type={noti.type}
                closable
                description={noti.message}
                onClose={() => removeNotification(noti)}
              />
            ))}

            <Flex className={styles.mapping}>
              <Suspense fallback={<Spin style={{ width: "100%" }} />}>
                {currentStep === 0 && (
                  <ControlPlaneUpgrade
                    isUpgrading={isUpgrading || isPendingControlPlaneUpgrade}
                    isUpgraded={isUpgraded}
                  />
                )}
                {currentStep === 1 && (
                  <StageUpgrade
                    stageEnvId={stageEnvId}
                    isUpgrading={isUpgrading || isPendingStageUpgrade}
                    upgradeVersion={templateDetail?.productVersion}
                    isUpgraded={isUpgraded}
                  />
                )}
                {currentStep === 2 && (
                  <ProductionUpgrade
                    stageEnvId={stageEnvId}
                    productEnvId={productEnvId}
                    isUpgrading={isUpgrading || isPendingProductionUpgrade}
                    isUpgraded={isUpgraded}
                  />
                )}
              </Suspense>
            </Flex>
          </Flex>
        </Spin>
      </Flex>

      <Flex justify="flex-end" gap={8}>
        <Button
          data-testid="btnClose"
          type="link"
          className={styles.btnClose}
          onClick={handleCancel}
        >
          Close
        </Button>
        <Button
          data-testid="btnUpgrade"
          type="primary"
          className={styles.btnUpgrade}
          disabled={isMappingIncomplete || isSendingUpgrade || !templateDetail || isUpgrading}
          loading={isCheckingStageUpgrade || isCheckingProductionUpgrade}
          onClick={startUpgrade}
        >
          {getUpgradeButtonText(isSendingUpgrade || isUpgrading, isUpgraded, currentStep)}
        </Button>
      </Flex>

      <StartUpgradeModal
        open={confirmUpgrade}
        onCancel={() => setConfirmUpgrade(false)}
        onOk={() => {
          setConfirmUpgrade(false);
          handleUpgrade();
        }}
      />
      <DeprecatedModal open={isTemplateDeprecated} onOk={() => handleCancel} />
      <IncompatibleMappingModal onCancel={handleCancel} />
    </PageLayout>
  );
}
