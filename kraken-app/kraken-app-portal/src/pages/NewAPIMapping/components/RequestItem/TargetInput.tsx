import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { IRequestMapping } from "@/utils/types/component.type";
import { RightOutlined } from "@ant-design/icons";
import { Flex, Tooltip } from "antd";
import clsx from "clsx";
import { isEqual } from "lodash";
import { useMemo } from "react";
import { LocationSelector } from "../LocationSelector";
import styles from "./index.module.scss";
import { AutoGrowingInput } from "@/components/form";
import { handleMappingInputChange } from "./InputCommon";

export function TargetInput({
  item,
  index,
}: Readonly<{
  item: IRequestMapping;
  index: number;
}>) {
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
      rightSide === EnumRightType.AddSellerProp &&
      isEqual(item.id, rightSideInfo?.previousData?.id),
    [rightSide, item.id, rightSideInfo?.previousData?.id]
  );

  const handleChange = (changes: { [field in keyof typeof item]?: any }) => {
    handleMappingInputChange(
      item,
      changes,
      {
        index,
        isFocused,
        requestMapping,
        rightSideInfo,
        setRequestMapping,
        setRightSideInfo
      }
    );
  };

  return (
    <Flex className={styles.flexColumn} gap={4}>
      {item.target ? (
        <LocationSelector
          type="request"
          // disabled={!item.customizedField}
          value={item.targetLocation}
          onChange={(value) => handleChange({ targetLocation: value })}
        />
      ) : <div className={styles.bloater}></div>}

      <Tooltip title={item.target}>
        <AutoGrowingInput
          data-testid="targetInput"
          id={JSON.stringify(item)}
          variant="filled"
          style={{ flex: 1 }}
          className={clsx(styles.sellerPropItemWrapper, {
            [styles.active]: isFocused,
            [styles.error]:
              errors?.requestIds?.has(item.id as any) && !item.target,
          })}
          value={item.target}
          placeholder="Select or input property"
          suffix={<RightOutlined style={{ fontSize: 12, color: "#C9CDD4" }} />}
          onClick={() => {
            setRightSide(EnumRightType.AddSellerProp);
            setRightSideInfo({
              method: "update",
              previousData: item,
              title: item.title,
            });
          }}
          onChange={(value) => {
            if (!value) {
              handleChange({ target: value, targetLocation: undefined });
            } else {
              handleChange({ target: value });
            }
          }}
        />
      </Tooltip>
    </Flex>
  );
}
