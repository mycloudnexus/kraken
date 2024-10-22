import { useDeleteApiServer } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IUnifiedAsset } from "@/utils/types/common.type";
import { Button, Tooltip } from "antd";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import APIServerDrawer from "../APIServerDrawer";
import TooltipDeleteBody from "./TooltipDeleteBody";
import TooltipInfoBody from "./TooltipInfoBody";
import styles from "./index.module.scss";
import { IComponent } from '@/utils/types/component.type';

type Item = IComponent | IUnifiedAsset | undefined;
type Props = {
  item: Item;
  isInEditMode?: boolean;
  refetchList?: () => void;
};

const DeleteApiButton = ({
  item,
  isInEditMode = false,
  refetchList,
}: Props) => {
  const [openTooltip, setOpenTooltip] = useState(false);
  const [openDrawer, setOpenDrawer] = useState(false);
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();
  const { mutateAsync: deleteApiServer } = useDeleteApiServer();

  const isApiInUse = useMemo(() => !!item?.inUse, [item]);
  const componentId = useMemo(() => item?.metadata?.key, [item?.metadata?.key]);

  const handleDelete = async () => {
    if (componentId) {
      await deleteApiServer({ productId: currentProduct, componentId } as any);
      setOpenTooltip(false);
      refetchList
        ? refetchList()
        : navigate(`/component/${currentProduct}/list`);
    }
  };

  const DeleteButton = (
    <Button
      type="text"
      style={{ color: isApiInUse ? "rgba(0,0,0,0.25)" : "red" }}
      onClick={() => setOpenTooltip(true)}
      disabled={isApiInUse}
    >
      Delete
    </Button>
  );

  return (
    <div>
      <Tooltip
        placement={isInEditMode ? "topLeft" : "bottomLeft"}
        align={{
          offset: [
            isApiInUse ? -40 : 35,
            isInEditMode ? (isApiInUse ? -10 : -20) : 20
          ],
        }}
        defaultOpen={false}
        open={openTooltip || undefined}
        title={
          isApiInUse
            ? TooltipInfoBody(setOpenDrawer, setOpenTooltip)
            : TooltipDeleteBody(handleDelete, setOpenTooltip)
        }
        rootClassName={styles.tooltip}
      >
        {isApiInUse && DeleteButton}
      </Tooltip>
      {!isApiInUse && DeleteButton}

      {componentId && openDrawer && (
        <APIServerDrawer
          onClose={() => setOpenDrawer(false)}
          isOpen={openDrawer}
          componentId={componentId}
        />
      )}
    </div>
  );
};

export default DeleteApiButton;
