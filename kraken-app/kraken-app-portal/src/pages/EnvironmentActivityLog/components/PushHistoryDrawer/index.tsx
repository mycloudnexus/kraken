import LogMethodTag from "@/components/LogMethodTag";
import renderRequiredMark from "@/components/RequiredFormMark";
import TrimmedPath from "@/components/TrimmedPath";
import { useGetProductEnvActivitiesMutation } from "@/hooks/product";
import { usePostPushActivityLog } from "@/hooks/pushApiEvent";
import { toDateTime } from "@/libs/dayjs";
import { useAppStore } from "@/stores/app.store";
import { TIME_ZONE_FORMAT } from "@/utils/constants/format";
import { getProductName } from "@/utils/helpers/name";
import { ICreateActivityHistoryLogRequest } from "@/utils/types/common.type";
import { CloseOutlined } from "@ant-design/icons";
import {
  DatePicker,
  Flex,
  Form,
  Radio,
  Drawer,
  Table,
  Button,
  Checkbox,
  message,
} from "antd";
import { ColumnsType } from "antd/es/table";
import dayjs from "dayjs";
import { capitalize } from "lodash";
import { useCallback, useEffect, useMemo } from "react";
import { Link } from "react-router-dom";
import styles from "./index.module.scss";

const dataSecurityAgreementEnabled =
  import.meta.env.VITE_ENABLE_DATA_SECURITY_AGREEMENT === "enabled";

type Props = {
  isOpen: boolean;
  onClose: () => void;
  envOptions: Array<{
    value: string;
    label: string;
  }>;
};

const PushHistoryDrawer = ({ isOpen, onClose, envOptions }: Props) => {
  const [form] = Form.useForm();
  const { currentProduct } = useAppStore();

  const { data: responseData, mutateAsync: getProductEnvActivities } =
    useGetProductEnvActivitiesMutation();
  const { mutateAsync: createPushActivityLog } = usePostPushActivityLog();

  const handleOK = () => {
    createPushActivityLog(parseParams())
      .then(() => message.success("Pushed successfully"))
      .catch(() => message.error("Pushed failed"));
    onClose();
  };

  const parseParams = (): ICreateActivityHistoryLogRequest => {
    const { envId, requestTime } = form.getFieldsValue();
    return {
      startTime: requestTime
        ? dayjs(requestTime).startOf("day").format(TIME_ZONE_FORMAT)
        : undefined,
      endTime: requestTime
        ? dayjs(requestTime).endOf("day").format(TIME_ZONE_FORMAT)
        : undefined,
      envId,
    };
  };

  const handleFormValuesChange = useCallback((t: any, values: any) => {
    if (t.path) return;
    const { requestTime = [] } = values ?? {};

    const params = {
      requestStartTime: requestTime
        ? dayjs(requestTime).startOf("day").format(TIME_ZONE_FORMAT)
        : undefined,
      requestEndTime: requestTime
        ? dayjs(requestTime).endOf("day").format(TIME_ZONE_FORMAT)
        : undefined,
    };

    if (values.envId && !!params.requestStartTime) {
      getProductEnvActivities({
        productId: currentProduct,
        envId: values.envId,
        params,
      });
    }
  }, []);

  useEffect(() => {
    const stageId = envOptions.find((key) => key.label === "stage")?.value;
    form.setFieldValue("envId", stageId);
  }, []);

  const isFormValid = useMemo(() => {
    const { envId, requestTime, termsOfUse } = form.getFieldsValue();
    return !!(
      responseData &&
      responseData.data.total > 0 &&
      envId &&
      requestTime &&
      (dataSecurityAgreementEnabled ? termsOfUse : true)
    );
  }, [responseData, form]);

  const columns: ColumnsType = [
    {
      title: "Product",
      dataIndex: "productType",
      render: (product) => getProductName(product),
    },
    {
      title: "Method",
      dataIndex: "method",
      render: (method) => <LogMethodTag method={method} />,
      width: 100,
    },
    {
      title: "Path",
      dataIndex: "path",
      render: (path) => (
        <Flex>
          <TrimmedPath path={path} />
        </Flex>
      ),
      width: 300,
    },
    {
      title: "Status",
      dataIndex: "httpStatusCode",
      width: 80,
    },
    {
      title: "Time",
      dataIndex: "createdAt",
      render: (time) => toDateTime(time),
    },
  ];

  return (
    <Drawer
      width={1000}
      open={isOpen}
      onClose={onClose}
      className={styles.modal}
      maskClosable={false}
      title={
        <Flex justify="space-between">
          <span>Push log</span>
          <CloseOutlined onClick={onClose} style={{ color: "#00000073" }} />
        </Flex>
      }
      footer={
        <Flex justify="flex-end" gap={10}>
          <Button onClick={onClose}>Cancel</Button>
          <Button
            type="primary"
            disabled={!isFormValid}
            data-testId="pushLog-btn"
            onClick={handleOK}
          >
            Ok
          </Button>
        </Flex>
      }
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleOK}
        onValuesChange={handleFormValuesChange}
        requiredMark={renderRequiredMark}
      >
        <Form.Item name="envId" label="Environment" required>
          <Radio.Group size="middle" name="Environment">
            {envOptions.map((key) => (
              <Radio key={key.value} value={key.value}>
                {capitalize(key.label)}
              </Radio>
            ))}
          </Radio.Group>
        </Form.Item>
        <Form.Item
          name="requestTime"
          label="Date"
          required
          className={styles.rangePicker}
        >
          <DatePicker maxDate={dayjs().endOf("day")} data-testId="datePicker" />
        </Form.Item>

        <Flex justify="center" style={{ width: "100%" }}>
          <Flex vertical gap={5} className={styles.tableContainer}>
            <span>
              Activity logs preview ({responseData?.data?.total || 0})
            </span>
            <Table
              className={styles.pushHistoryTable}
              tableLayout="fixed"
              dataSource={responseData?.data?.data}
              columns={columns}
            />
            {dataSecurityAgreementEnabled && (
              <Form.Item
                name="termsOfUse"
                valuePropName="checked"
                required
                style={{ marginBottom: "0px" }}
              >
                <Checkbox>
                  <Link
                    to="/terms-of-use.pdf"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    I accept the Data Transfer and Debugging Agreement
                  </Link>
                </Checkbox>
              </Form.Item>
            )}
          </Flex>
        </Flex>
      </Form>
    </Drawer>
  );
};

export default PushHistoryDrawer;
