import { Text } from "@/components/Text";
import { useGetErrorBrakedown } from "@/hooks/homepage";
import { useAppStore } from "@/stores/app.store";
import { formatDiagramDate } from "@/utils/constants/format";
import { ErrorBrakedown, IErrorBrakedown } from "@/utils/types/product.type";
import { Flex, Spin } from "antd";
import { useEffect, useMemo } from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { DiagramProps } from "..";
import styles from "../index.module.scss";
import NoData from '../NoData';

type Props = {
  props: DiagramProps;
};

const ErrorDiagram = ({ errorData }: { errorData: Array<unknown> }) => (
  <ResponsiveContainer width="100%" height="100%">
    <BarChart data={errorData}>
      <XAxis
        stroke="0px"
        dataKey="date"
        tick={{ fill: "#96A5B8" }}
        tickFormatter={formatDiagramDate}
      />
      <YAxis stroke="0px" tick={{ fill: "#96A5B8" }} />
      <Tooltip labelFormatter={formatDiagramDate} />
      <Legend
        formatter={(value: string) => (
          <span className={styles.errorBrakedownLegend}>{value}</span>
        )}
      />
      {["500", "404", "401", "400"].map((key, index) => (
        <Bar
          key={key}
          barSize={10}
          stackId="error"
          dataKey={key}
          fill={["#A8071A", "#F5222D", "#FF7875", "#FFF1F0"][index]}
          radius={key === "400" ? [10, 10, 0, 0] : 0}
        />
      ))}
    </BarChart>
  </ResponsiveContainer>
)

const ErrorBrakedownDiagram = ({ props }: Props) => {
  const { currentProduct } = useAppStore();
  const { data, isLoading, refetch, isRefetching } = useGetErrorBrakedown(
    currentProduct,
    props.envId,
    props.startTime,
    props.endTime
  );

  useEffect(() => {
    refetch();
  }, [props]);

  const processErrorData = (data: IErrorBrakedown | undefined) =>
    data?.errorBreakdowns.map((item) => ({
      ...item,
      ...item.errors,
      errors: undefined,
    })) || [];

  const errorData = useMemo(
    () => processErrorData(data),
    [data, isLoading]
  );

  return (
    <Flex vertical className={styles.contentBox}>
      <Flex style={{ paddingBottom: "12px" }}>
        <Text.LightMedium>Error brakedown</Text.LightMedium>
      </Flex>
      <Spin spinning={isLoading || isRefetching}>
        {!errorData.length
          ? <NoData description='When errors occur, they will be displayed here.' /> 
          : <ErrorDiagram errorData={errorData} />
        }
      </Spin>
    </Flex>
  );
};

export default ErrorBrakedownDiagram;
