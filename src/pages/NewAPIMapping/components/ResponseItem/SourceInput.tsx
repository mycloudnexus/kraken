import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { IResponseMapping } from "@/utils/types/component.type";
import { RightOutlined } from "@ant-design/icons";
import { Flex, Input, Tooltip } from "antd";
import clsx from "clsx";
import { cloneDeep, set } from "lodash";
import { useEffect, useState } from "react";
import { locationMapping } from "../../helper";
import { LocationSelector } from "../LocationSelector";
import styles from "./index.module.scss";

export function SourceInput({
  item,
  index,
  isSellerSideProps,
}: Readonly<{
  item: IResponseMapping;
  index: number;
  isSellerSideProps?: boolean;
}>) {
  const {
    responseMapping,
    activeResponseName,
    setRightSide,
    rightSide,
    setResponseMapping,
    setActiveResponseName,
    errors,
  } = useNewApiMappingStore();

  const [value, setValue] = useState("");

  const handleChange = (field: keyof typeof item, value: any) => {
    const cloneObj = cloneDeep(responseMapping);
    const itemIndex = cloneObj?.findIndex(
      (i) => i.name === item.name && i.target === item.target
    );
    set(cloneObj, `[${itemIndex}].${field}`, value);
    setResponseMapping(cloneObj);
  };

  useEffect(() => {
    setValue(item.source);
  }, [item.source, setValue]);

  const id = `${index}-${item?.name}`;
  const isFocused =
    rightSide === EnumRightType.AddSellerResponse && activeResponseName === id;

  return (
    <Flex className={styles.flexColumn} gap={4}>
      {item.source && (
        <LocationSelector
          type="response"
          disabled={!isSellerSideProps && !item.customizedField}
          value={locationMapping(item.sourceLocation)}
          onChange={(value) => handleChange("sourceLocation", value)}
        />
      )}

      <Tooltip title={item?.source}>
        <Input
          data-testid="sourceInput"
          id={id}
          variant="filled"
          placeholder="Select or input property"
          className={clsx(styles.input, {
            [styles.activeInput]: isFocused,
            [styles.error]: errors?.responseIds?.has(item.id!) && !item.source,
          })}
          value={value}
          suffix={<RightOutlined style={{ fontSize: 12, color: "#C9CDD4" }} />}
          onClick={() => {
            setActiveResponseName(id);
            setRightSide(EnumRightType.AddSellerResponse);
          }}
          onChange={(e) => setValue(e.target.value)}
          onBlur={() => {
            handleChange("source", value);
            // setActiveResponseName(undefined);
          }}
        />
      </Tooltip>
    </Flex>
  );
}
