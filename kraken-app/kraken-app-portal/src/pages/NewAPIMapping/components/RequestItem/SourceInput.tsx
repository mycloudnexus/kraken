import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { IRequestMapping } from "@/utils/types/component.type";
import { Flex, Tooltip } from "antd";
import clsx from "clsx";
import { isEqual, cloneDeep, set } from "lodash";
import { useMemo } from "react";
import { LocationSelector } from "../LocationSelector";
import styles from "./index.module.scss";
import { AutoGrowingInput } from "@/components/form";

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
      isEqual(item, rightSideInfo?.previousData),
    [rightSide, item, rightSideInfo?.previousData]
  );

  const handleChange = (changes: { [field in keyof typeof item]?: any }) => {
    const newRequest = cloneDeep(requestMapping);
    for (const field in changes) {
      set(
        newRequest,
        `[${index}].${field}`,
        changes[field as keyof typeof item]
      );
    }

    setRequestMapping(newRequest);
  };

  return (
    <Flex className={styles.flexColumn} gap={4}>
      {item.source ? (
        <LocationSelector
          type="request"
          disabled={!item.customizedField}
          value={item.sourceLocation}
          onChange={(value) => handleChange({ sourceLocation: value })}
        />
      ) : <div className={styles.bloater}></div>}

      <Tooltip title={item.source}>
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
      </Tooltip>
    </Flex>
  );
}
