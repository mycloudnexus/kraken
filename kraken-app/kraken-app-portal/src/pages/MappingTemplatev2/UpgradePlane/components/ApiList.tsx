import { ApiCard } from "@/components/ApiMapping";
import { Text } from "@/components/Text";
import { IRunningMapping } from "@/utils/types/env.type";
import { CheckCircleFilled } from "@ant-design/icons";
import { Flex, Tooltip } from "antd";
import classNames from "classnames";
import styles from "../index.module.scss";
import { ApiListSkeleton } from "./ApiListSkeleton";

function getMappingStatusIcon(status: string) {
  switch (status) {
    case "complete":
      return <CheckCircleFilled style={{ color: "var(--success)" }} />;
    case "incomplete":
      return (
        <Tooltip title="Incomplete mapping">
          <span className={styles.incompleteMappingBadge} />
        </Tooltip>
      );
    default:
      return <></>;
  }
}

export function ApiList({
  title,
  loading,
  details = [],
  highlights,
  indicators = [],
  statusIndicatorPosition,
  clickable,
  onItemClick,
}: Readonly<{
  title?: React.ReactNode;
  loading?: boolean;
  details?: IRunningMapping[];
  statusIndicatorPosition?: "left" | "right";
  highlights?: Record<string, any> // targetMapperKeys
  indicators?: string[] // mapping status
  clickable?: boolean
  onItemClick?: (item: IRunningMapping) => void;
}>) {
  return (
    <Flex className={styles.apiList}>
      <Text.NormalMedium
        data-testid="mappingListTitle"
        className={styles.listTitle}
      >
        {title}
      </Text.NormalMedium>

      {loading && <ApiListSkeleton />}

      {details.map((item, index) => (
        <ApiCard
          key={item.path + "_" + index}
          apiInstance={item as any}
          className={classNames(
            clickable && highlights?.[item.targetMapperKey] && styles.incompleteMapping
          )}
          prefix={
            statusIndicatorPosition === "left" &&
            indicators.includes(item.mappingStatus) &&
            getMappingStatusIcon(item.mappingStatus)
          }
          suffix={
            statusIndicatorPosition === "right" &&
            indicators.includes(item.mappingStatus) &&
            getMappingStatusIcon(item.mappingStatus)
          }
          mappingMatrixPosition="right"
          onClick={() => onItemClick?.(item)}
        />
      ))}
    </Flex>
  );
}
