import { IResponseMapping } from "@/utils/types/component.type";
import styles from "./index.module.scss";
import { Button, Flex, Input, Select, Tooltip, Typography } from "antd";
import Text from "@/components/Text";
import MappingIcon from "@/assets/newAPIMapping/mapping-icon-response.svg";
import { cloneDeep, difference, get, isEmpty, set } from "lodash";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import {
  CheckOutlined,
  CloseOutlined,
  DeleteOutlined,
  EditOutlined,
  RightOutlined,
} from "@ant-design/icons";
import { useBoolean } from "usehooks-ts";
import { useEffect, useState } from "react";
import { EnumRightType } from "@/utils/types/common.type";
import clsx from "clsx";

type Props = {
  item: IResponseMapping;
  index: number;
};

const ResponseItem = ({ item, index }: Props) => {
  const {
    responseMapping,
    setResponseMapping,
    listMappingStateResponse: listMapping,
    activeResponseName,
    setRightSide,
    setActiveResponseName,
    setListMappingStateResponse,
    setActiveSonataResponse,
    activeSonataResponse,
    rightSide,
  } = useNewApiMappingStore();

  const [titleInput, setTitleInput] = useState(item.title || "");
  const [descriptionInput, setDescriptionInput] = useState(item.description || "");

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

  useEffect(() => {
    setTitleInput(item.title || "");
    setDescriptionInput(item.description || "");
  }, [item.title, item.description]);

  const updateResponseMapping = (key: string, value: any) => {
    const newResponse = cloneDeep(responseMapping);
    set(newResponse, `[${index}].${key}`, value);
    setResponseMapping(newResponse);
  };

  const onChangeTitle = () => {
    updateResponseMapping("title", titleInput);
    disableEditTitle();
  };

  const onChangeDescription = () => {
    updateResponseMapping("description", descriptionInput);
    disableEditDescription();
  };

  const handleDeleteItemResponse = () => {
    const newResponse = cloneDeep(responseMapping);
    newResponse.splice(index, 1);
    setResponseMapping(newResponse);
  };

  const openSelectorForProp = (index?: number, name?: string) => {
    setActiveResponseName(`${index}-${name}`);
    setRightSide(EnumRightType.AddSellerResponse);
  };

  const handleAdd = (name: string) => {
    const newKey = get(listMapping, `[${listMapping.length - 1}].key`, 0) + 1;
    setListMappingStateResponse([
      ...listMapping,
      {
        key: newKey,
        from: undefined,
        to: undefined,
        name,
      },
    ]);
  };

  const handleDeleteMapping = (key: number) => {
    setListMappingStateResponse(listMapping.filter((item) => item.key !== key));
  };

  const handleSelect = (value: string, key: number) => {

    const cloneArr = cloneDeep(listMapping);
    const itemIndex = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${itemIndex}].from`, value);
    setListMappingStateResponse(cloneArr);
  };

  const handleChangeInput = (value: string[], key: number) => {

    const cloneArr = cloneDeep(listMapping);
    const itemIndex = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${itemIndex}].to`, value);
    setListMappingStateResponse(cloneArr);
  };

  const handleChangeResponse = (
    value: string,
    name: string,
    target: string
  ) => {

    const cloneObj = cloneDeep(responseMapping);
    const itemIndex = cloneObj?.findIndex(
      (i) => i.name === name && i.target === target
    );
    set(cloneObj, `[${itemIndex}].source`, value);
    set(cloneObj, `[${itemIndex}].sourceLocation`, `BODY`);
    setResponseMapping(cloneObj);
    setActiveResponseName(undefined);
  };

  return (
    <div
      className={clsx([
        styles.root,
        item.requiredMapping && styles.rootRequired,
      ])}
    >
      <Flex vertical gap={4} className={styles.header}>
        <Flex justify="space-between" align="center">
          {isEditTitle ? (
            <Flex align="center" gap={10} style={{ flex: 1 }}>
              <Input
                value={titleInput}
                onChange={(e) => setTitleInput(e.target.value)}
                style={{ width: "calc(50% - 30px)" }}
                allowClear
              />
              <CheckOutlined
                style={{ color: "#1890FF" }}
                onClick={onChangeTitle}
              />
              <CloseOutlined
                style={{ color: "#CF1322" }}
                onClick={() => {
                  setTitleInput(item.title);
                  disableEditTitle();
                }}
              />
            </Flex>
          ) : (
            <Flex align="center" gap={10}>
              <Text.NormalMedium>{item.title}</Text.NormalMedium>
              {Boolean(item?.customizedField) && (
                <EditOutlined
                  style={{ cursor: "pointer" }}
                  onClick={() => {
                    enableEditTitle();
                    disableEditDescription();
                  }}
                />
              )}
            </Flex>
          )}
          {item.requiredMapping && (
            <Text.LightSmall color="#FF9A2E">Required mapping</Text.LightSmall>
          )}
          {Boolean(item.customizedField) && (
            <Button type="link" onClick={handleDeleteItemResponse} danger>
              Delete
            </Button>
          )}
        </Flex>
        {isEditDescription ? (
          <Flex align="center" gap={10}>
            <Input
              value={descriptionInput}
              onChange={(e) => setDescriptionInput(e.target.value)}
              style={{ width: "calc(50% - 22px)" }}
              allowClear
            />
            <CheckOutlined
              style={{ color: "#1890FF" }}
              onClick={onChangeDescription}
            />
            <CloseOutlined
              style={{ color: "#CF1322" }}
              onClick={() => {
                setDescriptionInput(item.description);
                disableEditDescription();
              }}
            />
          </Flex>
        ) : (
          <Flex align="center" gap={10}>
            <Text.LightSmall color="#00000073">
              {item.description}
            </Text.LightSmall>
            {Boolean(item?.customizedField) && (
              <EditOutlined
                style={{ cursor: "pointer" }}
                onClick={() => {
                  enableEditDescription();
                  disableEditTitle();
                }}
              />
            )}
          </Flex>
        )}
      </Flex>
      <Flex className={styles.container} gap={8} wrap="wrap" align="center">
        {!item.customizedField ? (
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
        ) : (
          <Tooltip title={item?.target}>
            <Input
              placeholder="Select or input property"
              style={{
                width: "calc(50% - 30px)",
              }}
              variant="filled"
              className={clsx(styles.input, {
                [styles.activeInput]:
                  rightSide === EnumRightType.SonataResponse &&
                  activeSonataResponse === `${index}-${item?.name}`,
              })}
              value={isEmpty(item?.target) ? undefined : get(item, "target")}
              onClick={() => {
                setActiveSonataResponse(`${index}-${item.name}`);
                setRightSide(EnumRightType.SonataResponse);
              }}
              onChange={(e) => {
                updateResponseMapping("target", e?.target?.value ?? "");
              }}
            />
          </Tooltip>
        )}

        <MappingIcon />
        <Tooltip title={item?.source}>
          <Input
            variant="filled"
            placeholder="Select or input property"
            style={{
              width: "calc(50% - 30px)",
            }}
            className={clsx(styles.input, {
              [styles.activeInput]:
                rightSide === EnumRightType.AddSellerResponse &&
                activeResponseName === `${index}-${item?.name}`,
            })}
            value={isEmpty(item?.source) ? undefined : get(item, "source")}
            onClick={() => {
              openSelectorForProp(index, get(item, "name"));
            }}
            onChange={(e) =>
              handleChangeResponse(e.target.value, item?.name, item?.target)
            }
            suffix={
              <RightOutlined style={{ fontSize: 12, color: "#C9CDD4" }} />
            }
          />
        </Tooltip>
      </Flex>
      {!isEmpty(item?.targetValues) && (
        <Flex vertical gap={20} style={{ width: "100%", marginTop: 8 }}>
          {listMapping
            ?.filter((i) => i.name === item?.name)
            ?.map(({ key, from, to }) => (
              <Flex
                className={styles.itemContainer}
                key={`${item.title}-${item.name}-${key}`}
                gap={8}
                wrap="wrap"
                align="center"
              >
                <Flex
                  align="center"
                  gap={8}
                  style={{ width: "calc(50% - 30px)" }}
                >
                  <Select
                    className={styles.stateSelect}
                    data-testid="select-sonata-state"
                    onSelect={(v) => handleSelect(v, key)}
                    placeholder="State"
                    style={{ flex: 1 }}
                    value={from}
                    options={difference(
                      item?.targetValues,
                      listMapping
                        ?.filter((l) => l.name === item.name)
                        .map((item) => item.from)
                    ).map((item) => ({
                      title: item,
                      value: item,
                    }))}
                  />
                  <DeleteOutlined onClick={() => handleDeleteMapping(key)} />
                </Flex>
                <MappingIcon />
                <Select
                  popupClassName={styles.selectPopup}
                  mode="tags"
                  key={`enum-${key}`}
                  placeholder="Input seller order state, Enter for multiple states"
                  style={{ width: "calc(50% - 30px)" }}
                  value={to}
                  className={styles.stateSelect}
                  onChange={(e) => handleChangeInput(e, key)}
                />
              </Flex>
            ))}
          <Flex className={styles.itemContainer}>
            <Button
              type="primary"
              onClick={() => handleAdd(item?.name)}
              data-testid="btn-add-state"
            >
              + Add Value Mapping
            </Button>
          </Flex>
        </Flex>
      )}
    </div>
  );
};

export default ResponseItem;
