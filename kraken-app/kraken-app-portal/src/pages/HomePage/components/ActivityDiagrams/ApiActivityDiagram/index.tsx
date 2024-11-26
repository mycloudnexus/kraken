import { Text } from "@/components/Text";
import { useGetActivityRequests } from "@/hooks/homepage";
import { useAppStore } from "@/stores/app.store";
import { formatDiagramDate } from "@/utils/constants/format";
import { Flex, Spin } from "antd";
import { capitalize } from "lodash";
import { useEffect, useMemo } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { DiagramProps } from "..";
import styles from "../index.module.scss";
import NoData from '../NoData';
import { EmptyBin } from '../../Icon';

type Props = {
  props: DiagramProps;
};

const ApiActivityDiagram = ({ props }: Props) => {
  const { currentProduct } = useAppStore();
  const { data, isLoading, refetch, isRefetching } = useGetActivityRequests(
    currentProduct,
    props.envId,
    props.requestStartTime,
    props.requestEndTime,
    props.buyer
  );

  useEffect(() => {
    refetch();
  }, [props]);

  const activityData = useMemo(
    () => (data?.requestStatistics || []).map(entry => ({
        ...entry,
        error: entry.error || 0,
        success: entry.success || 0,
      })),
    [isLoading, data]
  );

  return (
    <Flex vertical className={styles.contentBox}>
      <Flex style={{ paddingBottom: "12px" }}>
        <Text.LightMedium>Requests</Text.LightMedium>
      </Flex>
      <Spin spinning={isLoading || isRefetching}>
        {!isLoading && !activityData
          ? <NoData icon={EmptyBin} />
          : <ResponsiveContainer width="100%" height="100%">
            <LineChart width={500} height={300} data={activityData}>
              <XAxis
                dataKey="date"
                tickFormatter={formatDiagramDate}
                tick={{ fill: "#96A5B8" }}
                strokeOpacity="0.2"
              />
              <YAxis
                tick={{ fill: "#96A5B8" }}
                strokeOpacity="0.2"
                label={{
                  value: "# of requests",
                  angle: -90,
                  position: "left",
                  offset: -12,
                }}
              />
              <Tooltip
                cursor={{ stroke: "#A3B5D6", fill: "transparent", strokeWidth: 4 }}
                labelFormatter={formatDiagramDate}
              />
              <Legend align="right" formatter={(value) => capitalize(value)} />
              <Line
                type="bumpX"
                dot={false}
                dataKey="success"
                strokeWidth={5}
                stroke="#7AD6BE"
              />
              <Line
                type="bumpX"
                dot={false}
                dataKey="error"
                strokeWidth={5}
                stroke="#EB5173"
              />
            </LineChart>
          </ResponsiveContainer>
        }
      </Spin>
    </Flex>
  );
};

export default ApiActivityDiagram;
