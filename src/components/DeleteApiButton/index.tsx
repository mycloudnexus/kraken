import { useAppStore } from "@/stores/app.store";
import { IUnifiedAsset } from "@/utils/types/common.type";
import { IComponent } from "@/utils/types/component.type";
import { Button, Tooltip } from "antd";
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import APIServerDrawer from "../APIServerDrawer";
import TooltipDeleteBody from "./TooltipDeleteBody";
import TooltipInfoBody from "./TooltipInfoBody";
import styles from "./index.module.scss";

type Item = IComponent | IUnifiedAsset | undefined;
type Props = {
  item: Item;
  openMappingDrawer: boolean;
  setOpenMappingDrawer: (value: boolean) => void;
  deleteCallback: (params: any) => Promise<void>;
  isInEditMode?: boolean;
  refetchList?: () => void;
};

const DeleteApiButton = ({
  item,
  openMappingDrawer,
  setOpenMappingDrawer,
  deleteCallback,
  isInEditMode = false,
  refetchList,
}: Props) => {
  const [openTooltip, setOpenTooltip] = useState(false);
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();

  const isApiInUse = useMemo(() => !!item?.inUse, [item]);
  const componentId = useMemo(() => item?.metadata?.key, [item?.metadata?.key]);

  const handleDelete = async () => {
    if (componentId) {
      setOpenTooltip(false);
      await deleteCallback({ productId: currentProduct, componentId } as any);
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

  const offsetValueX = isApiInUse ? -40 : 35;
  const offsetValueY = isInEditMode ? (isApiInUse ? -10 : -20) : 20;

  return (
    <div>
      <Tooltip
        rootClassName={styles.tooltip}
        placement={isInEditMode ? "topLeft" : "bottomLeft"}
        align={{
          offset: [offsetValueX, offsetValueY],
        }}
        defaultOpen={false}
        open={openTooltip || undefined}
        title={
          isApiInUse
            ? TooltipInfoBody(setOpenMappingDrawer, setOpenTooltip)
            : TooltipDeleteBody(handleDelete, setOpenTooltip)
        }
      >
        {isApiInUse && DeleteButton}
      </Tooltip>
      {!isApiInUse && DeleteButton}

      {componentId && openMappingDrawer && (
        <APIServerDrawer
          onClose={() => setOpenMappingDrawer(false)}
          isOpen={openMappingDrawer}
          componentId={componentId}
        />
      )}
    </div>
  );
};

export default DeleteApiButton;
