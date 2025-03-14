import { Text } from "@/components/Text";
import { parseDateStartOrEnd, recentXDays } from "@/utils/constants/format";
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
import { capitalize } from "lodash";
import { useEffect, useMemo, useState } from "react";
import ApiActivityDiagram from "./ApiActivityDiagram";
import ErrorBrakedownDiagram from "./ErrorDiagram";
import MostPopularEndpoints from "./MostPopularEndpoints";
import styles from "./index.module.scss";

export type DiagramProps = {
  envId: string;
  requestStartTime?: string;
  requestEndTime?: string;
  buyer?: string;
  requestTime?: any;
};

type Props = {
  envs: Array<IEnv>;
};

const { RangePicker } = DatePicker;

const ActivityDiagrams = ({ envs }: Props) => {
  const stageEnvId =
    envs?.find((env: IEnv) => env.name?.toLowerCase() === "production")?.id ??
    "";
  const [form] = Form.useForm();
  const [selectedRecentDate, setSelectedRecentDate] = useState<
    number | undefined
  >(7);
  const { requestStartTime, requestEndTime } = recentXDays(selectedRecentDate);

  const [params, setParams] = useState<DiagramProps>({
    envId: stageEnvId,
    requestStartTime,
    requestEndTime,
    buyer: undefined,
  });

  const handleFormValues = (_: unknown, values: DiagramProps) => {
    const { requestTime = [] } = values ?? {};
    if (requestTime?.[0]) setSelectedRecentDate(undefined);
    setParams({
      envId: values.envId || params.envId,
      buyer: values.buyer ?? params.buyer,
      requestStartTime:
        parseDateStartOrEnd(requestTime?.[0], "start") ??
        params.requestStartTime,
      requestEndTime:
        parseDateStartOrEnd(requestTime?.[1], "end") ?? params.requestEndTime,
    });
  };

  const envOptions = useMemo(() => {
    return (
      envs?.map((env) => ({
        value: env.id,
        label: capitalize(env.name),
      })) ?? []
    );
  }, [envs]);

  const setRecentDate = ({ target: { value } }: RadioChangeEvent) => {
    form.setFieldsValue({ requestTime: null });
    const { requestStartTime, requestEndTime } = recentXDays(value);

    setSelectedRecentDate(Number(value));
    setParams({
      ...params,
      requestStartTime,
      requestEndTime,
    });
  };

  useEffect(() => {
    // default filter values to first option each select box
    if (!form.getFieldValue("envId")) {
      form.setFieldValue("envId", envOptions[0]?.value);
    }
    if (!form.getFieldValue("buyer")) {
      form.setFieldValue("buyer", "ALL_BUYERS");
    }
  }, []);

  return (
    <Flex vertical className={styles.wrapper} justify="center">
      <Form
        initialValues={{ stageEnvId }}
        form={form}
        layout="inline"
        colon={false}
        onValuesChange={handleFormValues}
      >
        <Flex
          style={{ width: "100%", paddingBottom: "16px" }}
          align="center"
          justify="space-between"
        >
          <Flex gap={12} align="center">
            <Text.NormalLarge>API activity dashboard</Text.NormalLarge>
            <Form.Item name="envId">
              <Select
                className={styles.customSelectBox}
                value={form.getFieldValue("envId")}
                options={envOptions}
                popupMatchSelectWidth={false}
                size="middle"
                variant="borderless"
              />
            </Form.Item>
            <Form.Item name="buyer">
              <Select
                className={styles.customSelectBox}
                options={[{ value: "ALL_BUYERS", label: "All buyers" }]}
                popupMatchSelectWidth={false}
                size="middle"
                style={{ minWidth: 100 }}
                variant="borderless"
              />
            </Form.Item>
          </Flex>
          <Flex align="center">
            <Form.Item>
              <Radio.Group
                onChange={setRecentDate}
                value={selectedRecentDate}
                size="middle"
              >
                <Radio.Button value={7}>Recent 7 days</Radio.Button>
                <Radio.Button value={90} data-testid="recent-90-days">
                  Recent 3 months
                </Radio.Button>
              </Radio.Group>
            </Form.Item>
            <Form.Item name="requestTime">
              <RangePicker
                value={[
                  dayjs(params.requestStartTime),
                  dayjs(params.requestEndTime),
                ]}
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

export default ActivityDiagrams;
