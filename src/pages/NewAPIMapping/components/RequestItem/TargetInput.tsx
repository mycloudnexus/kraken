import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { RightOutlined } from "@ant-design/icons";
import { Flex, Tooltip, Input } from "antd";
import clsx from "clsx";
import { isEqual, cloneDeep, set } from "lodash";
import styles from "./index.module.scss";
import { IRequestMapping } from "@/utils/types/component.type";
import { SecondaryText } from "@/components/Text";
import { LocationSelector } from "../LocationSelector";
import { locationMapping } from "./util";
import { useEffect, useMemo, useState } from "react";

export function TargetInput({
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
  } = useNewApiMappingStore();
  const [value, setValue] = useState('')

  const isFocused = useMemo(() =>
    rightSide === EnumRightType.AddSellerProp &&
    isEqual(item, rightSideInfo?.previousData),
    [rightSide, item, rightSideInfo]
  )

  const handleChange = (field: keyof typeof item, value: any) => {
    const newRequest = cloneDeep(requestMapping);
    set(newRequest, `[${index}].${field}`, value);
    setRequestMapping(newRequest);
  }

  useEffect(() => {
    setValue(item.target)
  }, [item.target, setValue])

  return (
    <Flex className={styles.flexColumn} gap={4}>
      {item.targetLocation && (
        <SecondaryText.Normal data-testid="targetLocation">
          {locationMapping(item.targetLocation)}
        </SecondaryText.Normal>
      )}
      {item.customizedField && item.target && !item.targetLocation && !isFocused && (
        <LocationSelector
          type="request"
          onChange={value => handleChange('targetLocation', value)} />
      )}

      <Tooltip title={item.target}>
        <Input
          data-testid="targetInput"
          id={JSON.stringify(item)}
          variant="filled"
          style={{ flex: 1 }}
          className={clsx(styles.sellerPropItemWrapper, {
            [styles.active]: isFocused,
          })}
          value={value}
          placeholder="Select or input property"
          suffix={
            <RightOutlined style={{ fontSize: 12, color: "#C9CDD4" }} />
          }
          onClick={() => {
            setRightSide(EnumRightType.AddSellerProp);
            setRightSideInfo({
              method: "update",
              previousData: item,
              title: item.title,
            });
          }}
          onChange={(e) => setValue(e.target.value)}
          onBlur={() => handleChange('target', value)}
        />
      </Tooltip>
    </Flex>
  )
}