import { SecondaryText } from "@/components/Text";
import { EnumRightType } from "@/utils/types/common.type";
import { Typography, Flex, Tooltip, Input } from "antd";
import clsx from "clsx";
import { isEmpty } from "lodash";
import { locationMapping } from "../RequestItem/util";
import { IResponseMapping } from "@/utils/types/component.type";
import styles from "./index.module.scss";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { useEffect, useState } from "react";
import { LocationSelector } from "../LocationSelector";

export function TargetInput(
  { item, index, onChange }: Readonly<{ item: IResponseMapping; index: number; onChange?(key: string, value: any): void }>) {
  const {
    rightSide,
    activeSonataResponse,
    setRightSide,
    setActiveSonataResponse,
  } = useNewApiMappingStore();

  const [value, setValue] = useState('')

  useEffect(() => {
    setValue(item.target)
  }, [item.target, setValue])

  if (!item.customizedField) {
    return (
      <div className={styles.target}>
        <Typography.Text
          ellipsis={{ tooltip: item.target }}
          style={{ lineHeight: "32px", fontWeight: 400 }}
        >
          {!isEmpty(item?.target)
            ? item?.target
            : "No mapping to Sonata API is required"}
        </Typography.Text>
      </div>
    )
  }

  const id = `${index}-${item.name}`

  const isFocused = rightSide === EnumRightType.SonataResponse &&
    activeSonataResponse === id

  return (
    <Flex className={styles.flexColumn} gap={4}>
      {item.targetLocation && (<SecondaryText.Normal data-testid="targetLocation">
        {locationMapping(item.targetLocation)}
      </SecondaryText.Normal>)}

      {item.customizedField && item.target && !item.targetLocation && !isFocused && (
        <LocationSelector
          type="response"
          onChange={value => onChange?.('targetLocation', value)} />
      )}

      <Tooltip title={item?.target}>
        <Input
          data-testid="targetInput"
          placeholder="Select or input property"
          variant="filled"
          className={clsx(styles.input, {
            [styles.activeInput]: isFocused,
          })}
          value={value}
          onClick={() => {
            setActiveSonataResponse(id);
            setRightSide(EnumRightType.SonataResponse);
          }}
          onChange={(e) => setValue(e.target.value)}
          onBlur={() => {
            onChange?.("target", value)
            setActiveSonataResponse(undefined);
          }}
        />
      </Tooltip>
    </Flex>
  )
}