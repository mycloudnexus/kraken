import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { IRequestMapping } from "@/utils/types/component.type";
import { Flex, Tooltip, Input } from "antd";
import clsx from "clsx";
import { isEqual, cloneDeep, set } from "lodash";
import { useEffect, useMemo, useState } from "react";
import { locationMapping } from "../../helper";
import { LocationSelector } from "../LocationSelector";
import styles from "./index.module.scss";

export function SourceInput({
  item,
  index,
}: Readonly<{ item: IRequestMapping; index: number }>) {
  const [value, setValue] = useState("");

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

  useEffect(() => {
    setValue(item.source);
  }, [item.source, setValue]);

  return (
    <Flex className={styles.flexColumn} gap={4}>
      {item.source && (
        <LocationSelector
          type="request"
          disabled={!item.customizedField}
          value={locationMapping(item.sourceLocation, "request")}
          onChange={(value) => handleChange({ sourceLocation: value })}
        />
      )}

      <Tooltip title={item.source}>
        <Input
          data-testid="sourceInput"
          variant="filled"
          disabled={!item.customizedField}
          placeholder="Select or input property"
          className={clsx(styles.requestMappingItemWrapper, {
            [styles.active]: isFocused,
            [styles.error]:
              errors?.requestIds?.has(item.id as any) && !item.source,
          })}
          value={value}
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
          onChange={(e) => setValue(e.target.value)}
          onBlur={() => {
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
