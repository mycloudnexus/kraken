import { useEditComponent, useGetComponentDetail } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { Button, Drawer, Form, Spin } from "antd";
import { useBoolean } from "usehooks-ts";
import APIEditor from "./APIEditor";
import APIViewer from "./APIViewer";
import { CloseOutlined } from "@ant-design/icons";
import Flex from "../Flex";
import Text from "../Text";
import styles from "./index.module.scss";

type Props = {
  isOpen: boolean;
  refresh?: () => void;
  onClose?: () => void;
  id: string;
};

const APIServerModal = ({ id, isOpen, refresh, onClose }: Props) => {
  const { currentProduct } = useAppStore();

  const { mutateAsync: runUpdate, isPending } = useEditComponent();
  const { data: componentDetail, isLoading } = useGetComponentDetail(
    currentProduct,
    id
  );
  const { value: isEdit, setTrue: enableEdit } = useBoolean(false);
  const [form] = Form.useForm();

  return (
    <Drawer
      className={styles.drawer}
      width={"80vw"}
      closable={false}
      open={isOpen}
      footer={
        <Flex justifyContent="flex-end" gap={12}>
          <Button onClick={onClose}>Cancel</Button>
          <Button
            disabled={isPending}
            loading={isPending}
            type="primary"
            onClick={() => {
              if (isEdit) {
                form.submit();
              } else {
                onClose?.();
              }
            }}
          >
            OK
          </Button>
        </Flex>
      }
      title={
        <Flex justifyContent="space-between">
          <Text.NormalLarge>View API Server</Text.NormalLarge>
          <CloseOutlined onClick={onClose} style={{ color: "#00000073" }} />
        </Flex>
      }
    >
      <Spin spinning={isLoading}>
        {isEdit ? (
          <APIEditor
            detail={componentDetail}
            onClose={onClose}
            refresh={refresh}
            componentId={id}
            form={form}
            runUpdate={runUpdate}
          />
        ) : (
          <APIViewer
            detail={componentDetail}
            onClose={onClose}
            enableEdit={enableEdit}
          />
        )}
      </Spin>
    </Drawer>
  );
};

export default APIServerModal;
