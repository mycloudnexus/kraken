import { Steps } from "@/components/Steps";
import { Text } from "@/components/Text";
import { useUser } from "@/hooks/user/useUser";
import { toDateTime } from "@/libs/dayjs";
import { Deployment, IReleaseHistory } from "@/utils/types/product.type";
import { Button, Empty, Flex, StepProps } from "antd";
import classNames from "classnames";
import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { getUpgradeSteps } from "../../utils";
import styles from "./index.module.scss";

function getUpgradeEnv(envName: string): string {
  switch (envName) {
    case "CONTROL_PLANE":
      return "Control plane";
    case "stage":
      return "Data plane: Stage";
    case "production":
      return "Data plane: Production";
    default:
      return "";
  }
}

function getUpgradeStatus(
  upgradeStatus: string,
  process: Deployment[],
  index: number
): StepProps["status"] {
  if (
    upgradeStatus === "Upgrading" &&
    index > 0 &&
    process[index - 1].status === "SUCCESS" &&
    !process[index].status
  )
    return "process";

  if (!process[index].createdAt) return "wait";

  switch (process[index].status) {
    case "IN_PROCESS":
      return "process";
    case "ERROR":
      return "error";
    default:
      return "finish";
  }
}

export function UpgradeProcess({
  release,
  onViewDetail,
}: Readonly<{ release: IReleaseHistory; onViewDetail(deploymentId: string): void }>) {
  const navigate = useNavigate();
  const { findUserName } = useUser();

  const data = useMemo(
    () => getUpgradeSteps(release.deployments),
    [release.deployments]
  );

  return (
    <Flex vertical className={styles.upgradeStatuses}>
      <Text.NormalMedium
        data-testid="upgradeProcessTitle"
        className={styles.tabTitle}
      >
        Upgrade status
      </Text.NormalMedium>

      <main className={styles.statuses}>
        {!release.deployments.length ? (
          <Empty description="No upgrade" className={styles.empty} />
        ) : (
          <Steps
            direction="vertical"
            current={-1}
            size="small"
            items={data.map(
              ({ deploymentId, envName, upgradeBy, createdAt, updatedAt }, index) => {
                const status = getUpgradeStatus(release.status, data, index);

                return {
                  title: getUpgradeEnv(envName),
                  className: classNames(status === "process" && "upgrading"),
                  description: (
                    <>
                      <p className={styles.stepInfo}>
                        <span>By</span>
                        <span data-testid="upgradedBy">
                          {findUserName(upgradeBy)}
                        </span>
                      </p>
                      <p className={styles.stepInfo}>
                        <span>From</span>
                        <span data-testid="createdAt">
                          {createdAt ? toDateTime(createdAt) : "-"}
                        </span>
                      </p>
                      <p className={styles.stepInfo}>
                        <span>To</span>
                        <span data-testid="upgradedAt">
                          {updatedAt ? toDateTime(updatedAt) : "-"}
                        </span>
                      </p>
                    </>
                  ),
                  status,
                  onClick: () => status === "finish" && onViewDetail(deploymentId),
                };
              }
            )}
          />
        )}
      </main>

      {release.status !== "Upgraded" && release.status !== "Deprecated" && (
        <Button
          data-testid="btnCheckUpgrade"
          type="primary"
          onClick={() => navigate(`upgrade/${release.templateUpgradeId}`)}
        >
          {release.status === "Upgrading"
            ? "Continue upgrading"
            : "Check and upgrade"}
        </Button>
      )}
    </Flex>
  );
}
