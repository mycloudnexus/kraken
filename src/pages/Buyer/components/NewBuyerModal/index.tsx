import { Flex, Form, Input, Modal, Tooltip, notification } from "antd";
import styles from "./index.module.scss";
import { useAppStore } from "@/stores/app.store";
import { useCreateBuyer } from "@/hooks/product";
import { get } from "lodash";
import { useEffect, useState } from "react";
import TokenModal from "../TokenModal";
import { useBoolean } from "usehooks-ts";
import { IBuyer } from "@/utils/types/component.type";
import { InfoCircleOutlined } from "@ant-design/icons";

type Props = {
  open: boolean;
  onClose: () => void;
  currentEnv: string;
};

const NewBuyerModal = ({ open, onClose, currentEnv }: Props) => {
  const { currentProduct } = useAppStore();
  const [form] = Form.useForm();
  const { mutateAsync: createBuyer } = useCreateBuyer();
  const {
    value: isOpenToken,
    setTrue: enableToken,
    setFalse: disableToken,
  } = useBoolean(false);
  const [responseItem, setResponseItem] = useState<IBuyer>();

  useEffect(() => {
    if (currentEnv) {
      form.setFieldValue("envId", currentEnv);
    }
  }, [currentEnv]);

  const handleOk = async (values: any) => {
    try {
      const params: any = {
        productId: currentProduct,
        data: {
          ...values,
          envId: currentEnv,
        },
      };
      const res = await createBuyer(params);
      notification.success({ message: get(res, "message", "Success!") });
      onClose();
      setResponseItem(get(res, "data"));
      enableToken();
    } catch (error) {
      notification.error({
        message: get(error, "reason", "Error. Please try again"),
      });
    }
  };

  useEffect(() => {
    if (!open) {
      form.resetFields();
      if (currentEnv) {
        form.setFieldValue("envId", currentEnv);
      }
    }
  }, [open]);

  return (
    <>
      {responseItem && isOpenToken && (
        <TokenModal
          open={isOpenToken}
          onClose={disableToken}
          item={responseItem}
        />
      )}
      <Modal
        className={styles.modal}
        open={open}
        onCancel={onClose}
        title="Add new buyer"
        onOk={form.submit}
      >
        <Form form={form} layout="vertical" onFinish={handleOk}>
          <Form.Item
            name="buyerId"
            label={
              <Flex gap={4}>
                Company ID
                <Tooltip title="Please input buyer’s company ID in Seller’s legacy API platform">
                  <InfoCircleOutlined style={{ color: "#8C8C8C" }} />
                </Tooltip>
                <span style={{ color: "#FF4D4F" }}>*</span>
              </Flex>
            }
            rules={[
              {
                required: true,
                message:
                  "Please input buyer’s company ID in Seller’s legacy API platform",
              },
            ]}
          >
            <Input placeholder="Please enter" />
          </Form.Item>
          <Form.Item
            name="companyName"
            label={
              <Flex gap={4}>
                Company name
                <Tooltip title="Please input buyer’s company name in Seller’s legacy API platform">
                  <InfoCircleOutlined style={{ color: "#8C8C8C" }} />
                </Tooltip>
              </Flex>
            }
          >
            <Input placeholder="Please enter" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default NewBuyerModal;
