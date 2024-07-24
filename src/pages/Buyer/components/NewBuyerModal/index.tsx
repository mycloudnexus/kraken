import { Flex, Form, Input, Modal, Radio, notification } from "antd";
import styles from "./index.module.scss";
import { useAppStore } from "@/stores/app.store";
import { useCreateBuyer, useGetProductEnvs } from "@/hooks/product";
import Text from "@/components/Text";
import { get, sortBy } from "lodash";
import { useEffect, useMemo } from "react";

type Props = {
  open: boolean;
  onClose: () => void;
  currentEnv: string;
};

const NewBuyerModal = ({ open, onClose, currentEnv }: Props) => {
  const { currentProduct } = useAppStore();
  const { data } = useGetProductEnvs(currentProduct);
  const [form] = Form.useForm();
  const { mutateAsync: createBuyer } = useCreateBuyer();

  useEffect(() => {
    if (currentEnv) {
      form.setFieldValue("envId", currentEnv);
    }
  }, [currentEnv]);

  const handleOk = async (values: any) => {
    try {
      const params: any = {
        productId: currentProduct,
        data: values,
      };
      const res = await createBuyer(params);
      notification.success({ message: get(res, "message", "Success!") });
      onClose();
    } catch (error) {
      notification.error({
        message: get(error, "reason", "Error. Please try again"),
      });
    }
  };

  const envOptions = useMemo(() => {
    const envReverse = sortBy(get(data, "data", []), "name").reverse();
    return envReverse.map((item) => ({
      label: (
        <Text.LightMedium style={{ textTransform: "capitalize" }}>
          {item.name}
        </Text.LightMedium>
      ),
      value: item.id,
    }));
  }, [data?.data]);

  return (
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
              Company ID<span style={{ color: "#FF4D4F" }}> *</span>
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
          <Input placeholder="Please input buyer’s company ID in Seller’s legacy API platform" />
        </Form.Item>
        <Form.Item name="companyName" label="Company name">
          <Input placeholder="Please input buyer’s company ID in Seller’s legacy API platform" />
        </Form.Item>
        <Form.Item
          name="envId"
          label={
            <Flex gap={4}>
              Environment<span style={{ color: "#FF4D4F" }}> *</span>
            </Flex>
          }
          rules={[{ required: true, message: "Please select environment" }]}
        >
          <Radio.Group className={styles.radio} options={envOptions} />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default NewBuyerModal;
