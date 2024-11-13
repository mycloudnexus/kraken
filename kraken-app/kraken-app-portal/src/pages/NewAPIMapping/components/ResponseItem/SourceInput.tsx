import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { IResponseMapping } from "@/utils/types/component.type";
import { RightOutlined } from "@ant-design/icons";
import { Flex, Tooltip } from "antd";
import clsx from "clsx";
import { cloneDeep, set } from "lodash";
import { locationMapping } from "../../helper";
import { LocationSelector } from "../LocationSelector";
import styles from "./index.module.scss";
import { AutoGrowingInput } from "@/components/form";

export function SourceInput({
  item,
  index,
}: Readonly<{
  item: IResponseMapping;
  index: number;
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
  const handleChange = (field: keyof typeof item, value: any) => {
    const cloneObj = cloneDeep(responseMapping);
    const itemIndex = cloneObj?.findIndex(
      (i) => i.name === item.name && i.target === item.target
    );
    set(cloneObj, `[${itemIndex}].${field}`, value);
    setResponseMapping(cloneObj);
  };

  const id = `${index}-${item?.name}`;
  const isFocused =
    rightSide === EnumRightType.AddSellerResponse && activeResponseName === id;

  return (
    <Flex className={styles.flexColumn} gap={4}>
      {item.source ? (
        <LocationSelector
          type="response"
          // disabled={!item.customizedField}
          value={locationMapping(item.sourceLocation, "response")}
          onChange={(value) => handleChange("sourceLocation", value)}
        />
      ) : <div className={styles.bloater}></div>}

      <Tooltip title={item?.source}>
        <AutoGrowingInput
          data-testid="sourceInput"
          id={id}
          variant="filled"
          placeholder="Select or input property"
          className={clsx(styles.input, {
            [styles.activeInput]: isFocused,
            [styles.error]: errors?.responseIds?.has(item.id!) && !item.source,
          })}
          value={item.source}
          suffix={<RightOutlined style={{ fontSize: 12, color: "#C9CDD4" }} />}
          onClick={() => {
            setActiveResponseName(id);
            setRightSide(EnumRightType.AddSellerResponse);
          }}
          onChange={(value) => handleChange("source", value)}
        />
      </Tooltip>
    </Flex>
  );
}
