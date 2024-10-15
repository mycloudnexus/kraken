import { SecondaryText } from "@/components/Text";
import { EnumRightType } from "@/utils/types/common.type";
import { IRequestMapping } from "@/utils/types/component.type";
import { Flex, Tooltip, Input } from "antd";
import clsx from "clsx";
import { isEqual, cloneDeep, set } from "lodash";
import styles from "./index.module.scss";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { locationMapping } from "./util";
import { useEffect, useMemo, useState } from "react";
import { LocationSelector } from "../LocationSelector";

export function SourceInput({ item, index }: Readonly<{ item: IRequestMapping; index: number }>) {
  const [value, setValue] = useState('')

  const {
    requestMapping,
    setRightSide,
    setRightSideInfo,
    setRequestMapping,
    rightSideInfo,
    rightSide,
  } = useNewApiMappingStore();

  const isFocused = useMemo(() =>
    rightSide === EnumRightType.AddSonataProp &&
    isEqual(item, rightSideInfo?.previousData),
    [rightSide, item, rightSideInfo?.previousData])

  const handleChange = (field: keyof typeof item, value: any) => {
    const newRequest = cloneDeep(requestMapping);
    set(newRequest, `[${index}].${field}`, value);
    setRequestMapping(newRequest);
  }

  useEffect(() => {
    setValue(item.source)
  }, [item.source, setValue])

  return (
    <Flex className={styles.flexColumn} gap={4}>
      {item.sourceLocation && (
        <SecondaryText.Normal data-testid="sourceLocation">
          {locationMapping(item.sourceLocation)}
        </SecondaryText.Normal>
      )}

      {item.customizedField && item.source && !item.sourceLocation && !isFocused && (
        <LocationSelector
          type="request"
          onChange={value => handleChange('sourceLocation', value)} />
      )}

      <Tooltip title={item.source}>
        <Input
          data-testid="sourceInput"
          variant="filled"
          disabled={!item.customizedField}
          placeholder="Select or input property"
          className={clsx(styles.requestMappingItemWrapper, {
            [styles.active]: isFocused,
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
          onChange={e => setValue(e.target.value)}
          onBlur={() => handleChange('source', value)}
        />
      </Tooltip>
    </Flex>
  )
}