import { DatePicker, Flex, Form, Modal, Radio, Spin } from "antd";
import styles from "./index.module.scss";
import { useCallback } from "react";
import { capitalize } from 'lodash';
import dayjs from 'dayjs';
import { useGetProductEnvActivitiesMutation } from '@/hooks/product';
import { disabled7DaysDate } from '@/utils/helpers/date';
import { useAppStore } from "@/stores/app.store";

const { RangePicker } = DatePicker;

type Props = {
  isOpen: boolean;
  onClose: () => void;
  onOK: (value: string) => void;
  envOptions: Array<{
    value: string;
    label: string;
  }>;
};

const PushHistoryModal = ({
  isOpen,
  onClose,
  onOK,
  envOptions,
}: Props) => {
  const [form] = Form.useForm();
  const { currentProduct } = useAppStore();

  const handleOK = () => {
    // TODO:
    onOK(form.getFieldsValue());
  };

  const { data: responseData, mutateAsync, isPending, isSuccess } = useGetProductEnvActivitiesMutation();

  const handleFormValuesChange = useCallback(
    (t: any, values: any) => {
      if (t.path) return;
      const { requestTime = [] } = values ?? {};

      const params = {
        requestStartTime: requestTime?.[0]
          ? dayjs(requestTime[0]).startOf("day").valueOf()
          : undefined,
        requestEndTime: requestTime?.[1]
          ? dayjs(requestTime[1]).endOf("day").valueOf()
          : undefined
      }

      if (values.envId && !!params.requestStartTime) {
        mutateAsync({
          productId: currentProduct,
          envId: values.envId,
          params
        })
      }
    },
    []
  );

  return (
    <Modal
      open={isOpen}
      onCancel={onClose}
      onOk={handleOK}
      title="Push log"
      className={styles.modal}
      okButtonProps={{ disabled: !isSuccess, "data-testid": "pushLog-btn" }}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleOK}
        onValuesChange={handleFormValuesChange}
        requiredMark={(label, { required }) =>
          required ? (
            <Flex align="center" gap={4}>
              {label}{" "}
              <span className="required-label" style={{ color: "#FF4D4F" }}>
                *
              </span>
            </Flex>
          ) : (
            <span>{label}</span>
          )
        }>
        <Form.Item
          name="envId"
          label="Environment"
          required
        >
          <Radio.Group size="middle" name="Environment" >
            {envOptions.map((key) => (
              <Radio value={key.value}>{capitalize(key.label)}</Radio>
            ))}
          </Radio.Group>
        </Form.Item>
        <Form.Item
          name="requestTime"
          label="Time range"
          required
          className={styles.rangePicker}
        >
          <RangePicker placeholder={["Select time", "Select time"]} disabledDate={disabled7DaysDate} />
        </Form.Item>

        <Flex justify={isPending ?  "center" : "start"}>
          <Spin spinning={isPending}>
            {responseData?.data?.total && <Flex vertical className={styles.numberContainer} gap={5}>
              <div>
                Number of activity logs filtered
              </div>
              <div className={Number(responseData?.data?.total) > 0 ? styles.greaterThanZero : styles.equalToZero}>
                {responseData?.data?.total}
              </div>
            </Flex>
            }
          </Spin>
        </Flex>


      </Form>
    </Modal>
  );
};

export default PushHistoryModal;
