import { IBuyer } from "@/utils/types/component.type";
import { Button, Popconfirm, notification } from "antd";
import styles from "./index.module.scss";
import { useRegenToken } from "@/hooks/product";
import { get, isEmpty } from "lodash";
import { useAppStore } from "@/stores/app.store";
import { useState } from "react";
import TokenModal from "../TokenModal";

type Props = {
  buyer: IBuyer;
};

const RegenToken = ({ buyer }: Props) => {
  const { currentProduct } = useAppStore();
  const { mutateAsync: runGen } = useRegenToken();
  const [currentBuyer, setCurrentBuyer] = useState<IBuyer>();
  const handleGen = async (id: string) => {
    try {
      const res = await runGen({
        productId: currentProduct,
        id,
      } as any);
      setCurrentBuyer(get(res, "data"));
    } catch (error) {
      notification.error({
        message: get(error, "reason", "Error. Please try again"),
      });
    }
  };
  return (
    <>
      {!isEmpty(currentBuyer) && (
        <TokenModal
          open={!isEmpty(currentBuyer)}
          onClose={() => setCurrentBuyer(undefined)}
          item={currentBuyer}
        />
      )}
      <Popconfirm
        overlayClassName={styles.popconfirm}
        title={undefined}
        description={
          <>
            Are you sure to rotate token?
            <br />
            New token will be generated and the previous token will be expired
            immediately after this action.
          </>
        }
        onConfirm={() => handleGen(buyer.id)}
        okText="Rotate"
        cancelText="Cancel"
      >
        <Button
          style={{ padding: 0 }}
          type="link"
          disabled={buyer?.metadata?.status !== "activated"}
        >
          Rotate token
        </Button>
      </Popconfirm>
    </>
  );
};

export default RegenToken;
