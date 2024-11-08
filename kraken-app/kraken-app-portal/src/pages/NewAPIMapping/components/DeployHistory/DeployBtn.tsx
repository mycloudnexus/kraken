import DeployIcon from "@/assets/icon/deploy.svg";
import InformationModal from "@/components/DeployStage/InformationModal";
import { useDeployProduction } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IDeploymentHistory } from "@/utils/types/product.type";
import { notification, Tooltip, Button } from "antd";
import { get } from "lodash";
import { useState } from "react";
import { useBoolean } from "usehooks-ts";
import styles from "./index.module.scss";

export const DeploymentBtn = ({
  record,
  env,
}: {
  record: IDeploymentHistory;
  env: Record<string, string>;
}) => {
  const { currentProduct } = useAppStore();
  const { value: isOpen, setTrue: open, setFalse: close } = useBoolean(false);
  const [modalText, setModalText] = useState("");
  const { mutateAsync: runDeploy, isPending } = useDeployProduction();

  const handleClick = async () => {
    try {
      const res = await runDeploy({
        productId: currentProduct,
        data: {
          tagInfos: [
            {
              tagId: record?.tagId,
            },
          ],
          sourceEnvId: env?.stageId,
          targetEnvId: env?.productionId,
        },
      } as any);

      notification.success({ message: get(res, "message", "Success!") });
    } catch (error) {
      setModalText(get(error, "reason", "Error. Please try again"));
      open();
    }
  };
  return (
    <>
      <InformationModal modalText={modalText} open={isOpen} onClose={close} />
      <Tooltip title="Deploy to Production">
        <Button
          data-testid="btnDeployToProd"
          loading={isPending}
          disabled={!record?.verifiedStatus}
          type="text"
          className={styles.defaultBtn}
          onClick={handleClick}
        >
          <DeployIcon />
        </Button>
      </Tooltip>
    </>
  );
};
