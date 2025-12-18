import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { IResponseMapping } from "@/utils/types/component.type";
import { Flex } from "antd";
import { Text } from '@/components/Text'
import clsx from "clsx";
import { LocationSelector } from "../LocationSelector";
import styles from "./index.module.scss";
import { AutoGrowingInput } from "@/components/form";

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

  const id = `${index}-${item.name}`;

  const isFocused =
    rightSide === EnumRightType.SonataResponse && activeSonataResponse === id;
  return (
    <Flex className={styles.flexColumn} gap={4}>
      {item.target ? (
        <LocationSelector
          type="response"
          disabled={!item.customizedField}
          value={item.targetLocation}
          onChange={(value) => onChange?.("targetLocation", value)}
        />
      ) : <div className={styles.bloater}></div>}

      {!item.customizedField && (
        <Text.LightMedium ellipsis
          className={styles.input}
          style={{ padding: 7, minHeight: 36, borderRadius: 4, width: '100%' }}>{item.target}</Text.LightMedium>
      )}
      {item.customizedField && (
        <AutoGrowingInput
          data-testid="targetInput"
          disabled={!item.customizedField}
          placeholder="Select or input property"
          variant="filled"
          className={clsx(styles.input, {
            [styles.activeInput]: isFocused,
            [styles.error]: errors?.responseIds?.has(item.id!) && !item.target,
            [styles.disabled]: !item.customizedField,
          })}
          value={item.target}
          onClick={() => {
            setActiveSonataResponse(id);
            setRightSide(EnumRightType.SonataResponse);
          }}
          onChange={(value) => {
            onChange?.("target", value);
            // setActiveSonataResponse(undefined);
          }}
        />
      )}
    </Flex>
  );
}
