import { useGetComponentDetail } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { Modal, Spin } from "antd";
import { useBoolean } from "usehooks-ts";
import APIEditor from "./APIEditor";
import APIViewer from "./APIViewer";

type Props = {
  isOpen: boolean;
  refresh?: () => void;
  onClose?: () => void;
  id: string;
};

const APIServerModal = ({ id, isOpen, refresh, onClose }: Props) => {
  const { currentProduct } = useAppStore();
  const { data: componentDetail, isLoading } = useGetComponentDetail(currentProduct, id);
  const { value: isEdit, setTrue: enableEdit } = useBoolean(false);

  return (
    <Modal width={"80vw"} closable={false} open={isOpen} footer={<></>}>
      <Spin spinning={isLoading}>
        {isEdit ? (
          <APIEditor
            detail={componentDetail}
            onClose={onClose}
            refresh={refresh}
            componentId={id}
          />
        ) : (
          <APIViewer
            detail={componentDetail}
            onClose={onClose}
            enableEdit={enableEdit}
          />
        )}
      </Spin>
    </Modal>
  );
};

export default APIServerModal;
