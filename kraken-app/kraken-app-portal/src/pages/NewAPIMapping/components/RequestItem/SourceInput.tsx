import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { IRequestMapping } from "@/utils/types/component.type";
import { Flex } from "antd";
import clsx from "clsx";
import { isEqual, cloneDeep, set } from "lodash";
import { useMemo } from "react";
import { LocationSelector } from "../LocationSelector";
import styles from "./index.module.scss";
import { AutoGrowingInput } from "@/components/form";
import { Text } from "@/components/Text";
import { handleMappingInputChange } from "./InputCommon";

export function SourceInput({
  item,
  index,
}: Readonly<{ item: IRequestMapping; index: number }>) {
  const {
    requestMapping,
    setRightSide,
    setRightSideInfo,
    setRequestMapping,
    rightSideInfo,
    rightSide,
    errors,
  } = useNewApiMappingStore();

  const isFocused = useMemo(
    () =>
      rightSide === EnumRightType.AddSonataProp &&
      isEqual(item.id, rightSideInfo?.previousData?.id),
    [rightSide, item.id, rightSideInfo?.previousData?.id]
  );

  return (
    <Flex className={styles.flexColumn} gap={4}>
      {item.source ? (
        <LocationSelector
          type="request"
          disabled={!item.customizedField}
          value={item.sourceLocation}
          onChange={(value) => handleMappingInputChange(
            { sourceLocation: value },
            index,
            isFocused,
            requestMapping,
            item,
            rightSideInfo,
            setRequestMapping,
            setRightSideInfo)}
        />
      ) : <div className={styles.bloater}></div>}

      {!item.customizedField && (
        <Text.LightMedium ellipsis
          className={styles.requestMappingItemWrapper}
          style={{ padding: 7 }}>{item.source}</Text.LightMedium>
      )}
      {item.customizedField && (
        <AutoGrowingInput
          data-testid="sourceInput"
          variant="filled"
          disabled={!item.customizedField}
          placeholder="Select or input property"
          className={clsx(styles.requestMappingItemWrapper, {
            [styles.active]: isFocused,
            [styles.error]:
              errors?.requestIds?.has(item.id as any) && !item.source,
            [styles.disabled]: !item.customizedField,
          })}
          value={item.source}
          style={{ flex: 1 }}
          onClick={() => {
            if (item.requiredMapping) {
              return;
            }
            setRightSide(EnumRightType.AddSonataProp);
            setRightSideInfo({
              method: "update",
              title: item.title,
              previousData: item,
            });
          }}
          onChange={(value) => {
            if (!value) {
              handleChange({ source: value, sourceLocation: undefined });
            } else {
              handleChange({ source: value });
            }
          }}
        />
      )}
    </Flex>
  );
}
