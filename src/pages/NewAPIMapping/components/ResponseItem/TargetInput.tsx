import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { IResponseMapping } from "@/utils/types/component.type";
import { Flex, Tooltip, Input } from "antd";
import clsx from "clsx";
import { useEffect, useState } from "react";
import { locationMapping } from "../../helper";
import { LocationSelector } from "../LocationSelector";
import styles from "./index.module.scss";

export function TargetInput({
  item,
  index,
  onChange,
}: Readonly<{
  item: IResponseMapping;
  index: number;
  onChange?(key: string, value: any): void;
}>) {
  const {
    rightSide,
    activeSonataResponse,
    errors,
    setRightSide,
    setActiveSonataResponse,
  } = useNewApiMappingStore();

  const [value, setValue] = useState("");

  useEffect(() => {
    setValue(item.target);
  }, [item.target, setValue]);

  const id = `${index}-${item.name}`;

  const isFocused =
    rightSide === EnumRightType.SonataResponse && activeSonataResponse === id;

  return (
    <Flex className={styles.flexColumn} gap={4}>
      {item.target && (
        <LocationSelector
          type="response"
          disabled={!item.customizedField}
          value={locationMapping(item.targetLocation)}
          onChange={(value) => onChange?.("targetLocation", value)}
        />
      )}

      <Tooltip title={item?.target}>
        <Input
          data-testid="targetInput"
          disabled={!item.customizedField}
          placeholder="Select or input property"
          variant="filled"
          className={clsx(styles.input, {
            [styles.activeInput]: isFocused,
            [styles.error]: errors?.responseIds?.has(item.id!) && !item.target,
          })}
          value={value}
          onClick={() => {
            setActiveSonataResponse(id);
            setRightSide(EnumRightType.SonataResponse);
          }}
          onChange={(e) => setValue(e.target.value)}
          onBlur={() => {
            onChange?.("target", value);
            setActiveSonataResponse(undefined);
          }}
        />
      </Tooltip>
    </Flex>
  );
}
