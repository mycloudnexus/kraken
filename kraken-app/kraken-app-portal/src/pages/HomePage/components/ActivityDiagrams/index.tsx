import { Text } from "@/components/Text";
import {
  getCurrentTimeWithZone,
  recentXDays,
  TIME_ZONE_FORMAT,
} from "@/utils/constants/format";
import { IEnv } from "@/utils/types/env.type";
import {
  Col,
  DatePicker,
  Flex,
  Form,
  Radio,
  RadioChangeEvent,
  Row,
  Select,
} from "antd";
import dayjs from "dayjs";
import { useCallback, useMemo, useState } from "react";
import ApiActivityDiagram from "./ApiActivityDiagram";
import ErrorBrakedownDiagram from "./ErrorDiagram";
import MostPopularEndpoints from "./MostPopularEndpoints";
import styles from "./index.module.scss";

export type DiagramProps = {
  envId: string;
  requestStartTime: string;
  requestEndTime: string;
  buyer: string | undefined;
  requestTime?: any;
};

type Props = {
  envs: Array<IEnv>;
};

const { RangePicker } = DatePicker;

const DiagramWrapper = ({ envs }: Props) => {
  const stageEnvId =
    envs?.find((env: IEnv) => env.name?.toLowerCase() === "stage")?.id ?? "";
  const currentTime = getCurrentTimeWithZone();
  const [form] = Form.useForm();
  const [params, setParams] = useState<DiagramProps>({
    envId: stageEnvId,
    requestStartTime: currentTime,
    requestEndTime: currentTime,
    buyer: undefined,
  });

  const handleFormValues = useCallback(
    (values: DiagramProps) => {
      const { requestTime = [] } = values ?? {};
      setParams({
        envId: values.envId || params.envId,
        buyer: values.buyer || params.buyer,
        requestStartTime: requestTime?.[0]
          ? dayjs(requestTime[0]).startOf("day").format(TIME_ZONE_FORMAT)
          : currentTime,
        requestEndTime: requestTime?.[1]
          ? dayjs(requestTime[1]).endOf("day").format(TIME_ZONE_FORMAT)
          : currentTime,
      });
    },
    [setParams, params]
  );

  const handleFormValuesChange = useCallback(
    (t: any, values: any) => {
      if (t.path) return;
      handleFormValues(values);
    },
    [setParams]
  );

  const envOptions = useMemo(() => {
    return (
      envs?.map((env) => ({
        value: env.id,
        label: env.name,
      })) ?? []
    );
  }, [envs]);

  const setRecentDate = (e: RadioChangeEvent) => {
    const { requestStartTime, requestEndTime } = recentXDays(e);
    setParams({ ...params, requestStartTime, requestEndTime });
  };

  return (
    <Flex vertical className={styles.wrapper} justify="center">
      <Form
        initialValues={{ stageEnvId }}
        form={form}
        layout="inline"
        colon={false}
        onValuesChange={handleFormValuesChange}
      >
        <Flex
          style={{ width: "100%", paddingBottom: "16px" }}
          align="center"
          justify="space-between"
        >
          <Flex gap={12} align="center">
            <Text.BoldMedium>Activity diagrams</Text.BoldMedium>

            <Form.Item name="envId">
              <Select
                value={params.envId}
                options={envOptions}
                popupMatchSelectWidth={false}
                size="middle"
                variant="borderless"
                placeholder="Stage"
              />
            </Form.Item>

            <Form.Item name="buyer">
              <Select
                options={[
                  {
                    value: "all",
                    label: "All buyers",
                  },
                ]}
                value={params.buyer}
                placeholder="All buyers"
                popupMatchSelectWidth={false}
                size="middle"
                style={{ minWidth: 100 }}
                variant="borderless"
                allowClear
              />
            </Form.Item>
          </Flex>
          <Flex align="center">
            <Form.Item>
              <Radio.Group onChange={setRecentDate} size="middle">
                <Radio.Button value="7">Recent 7 days</Radio.Button>
                <Radio.Button value="90">Recent 3 months</Radio.Button>
              </Radio.Group>
            </Form.Item>
            <Form.Item name="requestTime">
              <RangePicker
                size="middle"
                placeholder={["Select time", "Select time"]}
              />
            </Form.Item>
          </Flex>
        </Flex>
      </Form>
      <Flex vertical gap={16}>
        <ApiActivityDiagram props={params} />
        <Row gutter={[16, 16]}>
          <Col md={24} lg={12}>
            <ErrorBrakedownDiagram props={params} />
          </Col>
          <Col md={24} lg={12}>
            <MostPopularEndpoints props={params} />
          </Col>
        </Row>
      </Flex>
    </Flex>
  );
};

export default DiagramWrapper;
