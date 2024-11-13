import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { IResponseMapping } from "@/utils/types/component.type";
import { Flex, Tooltip } from "antd";
import clsx from "clsx";
import { locationMapping } from "../../helper";
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
          value={locationMapping(item.targetLocation, "response")}
          onChange={(value) => onChange?.("targetLocation", value)}
        />
      ) : <div className={styles.bloater}></div>}

      <Tooltip title={item?.target}>
        <AutoGrowingInput
          data-testid="targetInput"
          disabled={!item.customizedField}
          placeholder="Select or input property"
          variant="filled"
          className={clsx(styles.input, {
            [styles.activeInput]: isFocused,
            [styles.error]: errors?.responseIds?.has(item.id!) && !item.target,
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
      </Tooltip>
    </Flex>
  );
}
