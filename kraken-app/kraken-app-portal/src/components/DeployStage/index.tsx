import { Button, notification } from "antd";
import InformationModal from "./InformationModal";
import { useBoolean } from "usehooks-ts";
import { useMemo, useState } from "react";
import {
  PRODUCT_CACHE_KEYS,
  useDeployToEnv,
  useGetProductEnvs,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { get } from "lodash";
import { queryClient } from "@/utils/helpers/reactQuery";
import { useParams } from "react-router-dom";

type Props = {
  inComplete: boolean;
  diffWithStage: boolean;
  metadataKey: string;
};

const DeployStage = ({ inComplete, diffWithStage, metadataKey }: Props) => {
  const { componentId } = useParams();
  const { currentProduct } = useAppStore();
  const { value: isOpen, setTrue: open, setFalse: close } = useBoolean(false);
  const { mutateAsync: runDeploy, isPending } = useDeployToEnv();
  const [modalText, setModalText] = useState("");
  const { data: dataEnv } = useGetProductEnvs(currentProduct);
  const stageId = useMemo(() => {
    const stage = dataEnv?.data?.find(
      (env: any) => env.name?.toLowerCase() === "stage"
    );
    return stage?.id;
  }, [dataEnv]);
  const handleClick = async () => {
    if (!metadataKey || !stageId || !componentId) {
      notification.warning({
        message: "Please try again.",
      });
      return;
    }
    if (inComplete) {
      setModalText("Mapping is incomplete.");
      open();
      return;
    }
    if (!diffWithStage) {
      setModalText("No difference with currently running version in Stage.");
      open();
      return;
    }
    try {
      const res = await runDeploy({
        productId: currentProduct,
        componentId,
        mapperKeys: [metadataKey],
        envId: stageId,
      } as any);
      queryClient.invalidateQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_component_detail_mapping],
      });
      notification.success({ message: get(res, "message", "Success!") });
    } catch (error) {
      const errorMessage = get(error, "response.data.reason") ?? 'Error. Please try again';
      setModalText(errorMessage);
      open();
    }
  };
  return (
    <>
      <InformationModal modalText={modalText} open={isOpen} onClose={close} />
      <Button
        data-testid="deploy-to-stage"
        type="primary"
        onClick={handleClick}
        loading={isPending}
      >
        Deploy to stage
      </Button>
    </>
  );
};

export default DeployStage;
