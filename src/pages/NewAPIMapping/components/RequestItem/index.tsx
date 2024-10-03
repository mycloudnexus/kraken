import { IRequestMapping } from "@/utils/types/component.type";
import styles from "./index.module.scss";
import { Button, Flex, Input, Tooltip } from "antd";
import Text from "@/components/Text";
import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import clsx from "clsx";
import { cloneDeep, get, isEqual, set } from "lodash";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import {
  CheckOutlined,
  CloseOutlined,
  EditOutlined,
  RightOutlined,
} from "@ant-design/icons";
import { useBoolean } from "usehooks-ts";
import { useEffect, useState } from "react";

type Props = {
  item: IRequestMapping;
  index: number;
};

const RequestItem = ({ item, index }: Props) => {
  const {
    requestMapping,
    setRightSide,
    setRightSideInfo,
    setRequestMapping,
    rightSideInfo,
    rightSide,
  } = useNewApiMappingStore();
  const [titleInput, setTitleInput] = useState("");
  const [descriptionInput, setDescriptionInput] = useState("");
  const {
    value: isEditTitle,
    setTrue: enableEditTitle,
    setFalse: disableEditTitle,
  } = useBoolean(false);
  const {
    value: isEditDescription,
    setTrue: enableEditDescription,
    setFalse: disableEditDescription,
  } = useBoolean(false);

  const onChangeTitle = () => {
    const newRequest = cloneDeep(requestMapping);
    set(newRequest, `[${index}].title`, titleInput);
    setRequestMapping(newRequest);
    disableEditTitle();
  };

  useEffect(() => {
    if (!titleInput) {
      setTitleInput(item.title);
    }
    if (!descriptionInput) {
      setDescriptionInput(item.description);
    }
  }, [item.title]);

  const onChangeDescription = () => {
    const newRequest = cloneDeep(requestMapping);
    set(newRequest, `[${index}].description`, descriptionInput);
    setRequestMapping(newRequest);
    disableEditDescription();
  };

  const handleDelete = () => {
    const newRequest = cloneDeep(requestMapping);
    newRequest.splice(index, 1);
    setRequestMapping(newRequest);
  };

  return (
    <div
      className={clsx([
        styles.root,
        item.requiredMapping && styles.rootRequired,
      ])}
      id={JSON.stringify(item)}
    >
      <Flex vertical gap={4} className={styles.header}>
        <Flex align="center" justify="space-between">
          {isEditTitle ? (
            <Flex align="center" gap={10} style={{ flex: 1 }}>
              <Input
                onChange={(e) => setTitleInput(e.target.value)}
                value={titleInput}
                allowClear
                style={{ width: "calc(50% + 15px)" }}
              />
              <CheckOutlined
                onClick={onChangeTitle}
                style={{ color: "#1890FF" }}
              />
              <CloseOutlined
                onClick={() => {
                  setTitleInput(item.title);
                  disableEditTitle();
                }}
                style={{ color: "#CF1322" }}
              />
            </Flex>
          ) : (
            <Flex gap={10} align="center">
              <Text.NormalMedium>{item.title}</Text.NormalMedium>
              {Boolean(item?.customizedField) && (
                <EditOutlined
                  onClick={() => {
                    enableEditTitle();
                    disableEditDescription();
                  }}
                  style={{ cursor: "pointer" }}
                />
              )}
            </Flex>
          )}
          {Boolean(item.customizedField) && (
            <Button type="link" onClick={handleDelete} danger>
              Delete
            </Button>
          )}
          {item.requiredMapping && (
            <Text.LightSmall color="#FF9A2E">Required mapping</Text.LightSmall>
          )}
        </Flex>
        {isEditDescription ? (
          <Flex align="center" gap={10}>
            <Input
              onChange={(e) => setDescriptionInput(e.target.value)}
              value={descriptionInput}
              allowClear
              style={{ width: "calc(50% - 22px)" }}
            />
            <CheckOutlined
              onClick={onChangeDescription}
              style={{ color: "#1890FF" }}
            />
            <CloseOutlined
              onClick={() => {
                setDescriptionInput(item.description);
                disableEditDescription();
              }}
              style={{ color: "#CF1322" }}
            />
          </Flex>
        ) : (
          <Flex gap={10} align="center">
            <Text.LightSmall color="#00000073">
              {item.description}
            </Text.LightSmall>
            {Boolean(item?.customizedField) && (
              <EditOutlined
                onClick={() => {
                  enableEditDescription();
                  disableEditTitle();
                }}
                style={{ cursor: "pointer" }}
              />
            )}
          </Flex>
        )}
      </Flex>
      <Flex className={styles.container} gap={8} wrap="wrap">
        <Tooltip title={item.source}>
          <Input
            variant="filled"
            disabled={!item.customizedField}
            placeholder="Select or input property"
            className={clsx(styles.requestMappingItemWrapper, {
              [styles.active]:
                rightSide === EnumRightType.AddSonataProp &&
                isEqual(item, rightSideInfo?.previousData),
            })}
            value={item.source}
            style={{ width: "calc(50% - 22px)" }}
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
            onChange={(e) => {
              const newValue = get(e, "target.value", "")
                .replace?.("@{{", "")
                .replace?.("}}", "");
              let sourceLocation = get(item, "sourceLocation", "");
              if (newValue.includes(".")) {
                const splited = newValue.split(".");
                const pathValue = get(splited, "[0]", "").toLocaleUpperCase();
                sourceLocation =
                  pathValue === "REQUESTBODY" ? "BODY" : pathValue;
              }
              const newRequest = cloneDeep(requestMapping);
              set(newRequest, `[${index}].source`, get(e, "target.value", ""));
              set(newRequest, `[${index}].sourceLocation`, sourceLocation);
              setRequestMapping(newRequest);
            }}
          />
        </Tooltip>
        <MappingIcon />
        <Tooltip title={item.target}>
          <Input
            variant="filled"
            style={{ width: "calc(50% - 30px)" }}
            className={clsx(styles.sellerPropItemWrapper, {
              [styles.active]:
                rightSide === EnumRightType.AddSellerProp &&
                isEqual(item, rightSideInfo?.previousData),
            })}
            onClick={() => {
              setRightSide(EnumRightType.AddSellerProp);
              setRightSideInfo({
                method: "update",
                previousData: item,
                title: item.title,
              });
            }}
            value={item.target}
            placeholder="Select or input property"
            suffix={
              <RightOutlined style={{ fontSize: 12, color: "#C9CDD4" }} />
            }
            onChange={(e) => {
              if (e.target?.value?.startsWith("hybrid.")) {
                const newRequest = cloneDeep(requestMapping);
                set(
                  newRequest,
                  `[${index}].target`,
                  get(e, "target.value", "")
                );
                set(newRequest, `[${index}].targetLocation`, "HYBRID");
                setRequestMapping(newRequest);
                return;
              }
              const newValue = get(e, "target.value", "")
                .replace?.("@{{", "")
                .replace?.("}}", "");
              let targetLocation = get(item, "targetLocation", "");
              if (newValue.includes(".")) {
                const splited = newValue.split(".");
                const pathValue = get(splited, "[0]", "").toLocaleUpperCase();
                targetLocation =
                  pathValue === "REQUESTBODY" ? "BODY" : pathValue;
              }
              const newRequest = cloneDeep(requestMapping);
              set(newRequest, `[${index}].target`, get(e, "target.value", ""));
              set(newRequest, `[${index}].targetLocation`, targetLocation);
              setRequestMapping(newRequest);
            }}
          />
        </Tooltip>
      </Flex>
    </div>
  );
};

export default RequestItem;
