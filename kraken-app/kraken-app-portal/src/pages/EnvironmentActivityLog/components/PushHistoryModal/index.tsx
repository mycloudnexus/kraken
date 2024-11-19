import { DatePicker, Flex, Form, Modal, Radio } from "antd";
import styles from "./index.module.scss";
import { useCallback, useState } from "react";
import { capitalize } from 'lodash';
import dayjs from 'dayjs';
import { useGetProductEnvActivities } from '@/hooks/product';
import { disabled7DaysDate } from '@/utils/helpers/date';

const { RangePicker } = DatePicker;

type Props = {
  isOpen: boolean;
  onClose: () => void;
  onOK: (value: string) => void;
  envOptions: Array<{
    value: string;
    label: string;
  }>;
  queryParams: {
    productId: string,
    envId: string,
    params: Omit<Record<string, any>, "envId">,
  };
};

const PushHistoryModal = ({
  isOpen,
  onClose,
  onOK,
  envOptions,
  queryParams,
}: Props) => {
  const [form] = Form.useForm();

  const handleOK = () => {
    // TODO:
    onOK(form.getFieldsValue());
  };

  const { productId, envId, params: { requestStartTime, requestEndTime } } = queryParams;
  const [params, setParams] = useState({
    envId,
    requestStartTime,
    requestEndTime,
  });

  const { data, isLoading } = useGetProductEnvActivities(
    productId,
    params.envId,
    { requestStartTime: params.requestStartTime, requestEndTime: params.requestEndTime },
    'modal-cache'
  );

  const handleFormValuesChange = useCallback(
    (t: any, values: any) => {
      if (t.path) return;
      const { requestTime = [] } = values ?? {};

      const params = values;
      params.requestStartTime = requestTime?.[0]
        ? dayjs(requestTime[0]).startOf("day").valueOf()
        : undefined;
      params.requestEndTime = requestTime?.[1]
        ? dayjs(requestTime[1]).endOf("day").valueOf()
        : undefined;
      
      setParams(params)
    },
    [setParams]
  );

  return (
    <Modal
      open={isOpen}
      onCancel={onClose}
      onOk={handleOK}
      title="Push log"
      className={styles.modal}
      okButtonProps={{ "data-testid": "pushLog-btn" }}
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
          <Radio.Group defaultValue={params.envId} size="middle" name="Environment" >
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
   
        {!isLoading && <Flex vertical className={styles.numberContainer} gap={5}>
          <div>
            Number of activity logs filtered
          </div>
          <div className={Number(data?.total) > 0 ? styles.greaterThanZero : styles.equalToZero}>
            {data?.total}
          </div>

        </Flex>
        }
      </Form>
    </Modal>
  );
};

export default PushHistoryModal;
