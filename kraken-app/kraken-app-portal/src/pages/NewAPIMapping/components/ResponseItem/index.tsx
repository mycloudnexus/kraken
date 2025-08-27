import MappingIcon from "@/assets/newAPIMapping/mapping-icon-response.svg";
import { Text } from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { IResponseMapping } from "@/utils/types/component.type";
import { IMapping } from "@/pages/NewAPIMapping/components/ResponseMapping";
import {
  CheckOutlined,
  CloseOutlined,
  DeleteOutlined,
  EditOutlined,
} from "@ant-design/icons";
import { Button, Flex, Input, Select } from "antd";
import clsx from "clsx";
import { cloneDeep, difference, isEmpty, set } from "lodash";
import { nanoid } from "nanoid";
import { useEffect, useState } from "react";
import { useBoolean } from "usehooks-ts";
import { SourceInput } from "./SourceInput";
import { TargetInput } from "./TargetInput";
import { handleDeleteMappingItems } from "@/pages/NewAPIMapping/helper";
import styles from "./index.module.scss";

type Props = {
  item: IResponseMapping;
  index: number;
};

const ResponseItem = ({ item, index }: Props) => {
  const {
    responseMapping,
    setResponseMapping,
    listMappingStateResponse: listMapping,
    setListMappingStateResponse,
  } = useNewApiMappingStore();

  const [titleInput, setTitleInput] = useState(item.title || "");
  const [descriptionInput, setDescriptionInput] = useState(
    item.description || ""
  );

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

  const handleAdd = (name: string) => {
    setListMappingStateResponse([
      ...listMapping,
      {
        key: nanoid(),
        from: undefined,
        to: [],
        name,
        groupId: item.name 
        ?? (item.title && item.title !== "Title of Property Mapping"
          ? `${item.title}-${index}`
          : `custom-${index}-${nanoid()}`)
      },
    ]);
  };
  
  const handleDeleteMapping = (key: React.Key) => {
    const updated = handleDeleteMappingItems(key, listMapping, []);
    setListMappingStateResponse(updated);
  };

  
  const handleSelect = (value: string, key: React.Key) => {
    const cloneArr = cloneDeep(listMapping);
    const itemIndex = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${itemIndex}].from`, value);
    setListMappingStateResponse(cloneArr);
  };

  const handleChangeInput = (value: string[], key: React.Key) => {
    const cloneArr = cloneDeep(listMapping);
    const itemIndex = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${itemIndex}].to`, value);
    setListMappingStateResponse(cloneArr);
  };

  const handleDeleteMappingCustomized = (key: React.Key) => {
    const updated = handleDeleteMappingItems(key, listMapping, undefined);
    console.log("handleDeleteMappingCustomized updated", updated);
    setListMappingStateResponse(updated);
    updateTargetValuesAndMapping(updated);
  };

  // Handle customized descrete enum input
  const updateTargetValuesAndMapping = (updatedList: IMapping[]) => {
    // Fallback groupId logic (handle historical data)
    const groupId =
    item.name ??
    (item.title && item.title !== "Title of Property Mapping"
      ? `${item.title}-${index}`
      : `custom-${index}-${nanoid()}`);

    // Filter only mappings belonging to this group
    const rows = updatedList.filter((m) => m.groupId === groupId);

    console.log("pdateTargetValuesAndMapping rows", rows);
    const targetValues = rows
      .map((m) => m.to?.[0]?.trim())
      .filter((v): v is string => Boolean(v));
  
    const valueMapping: Record<string, string> = {};
    rows.forEach((m) => {
      const from = m.from?.trim();
      // take first element of string[]
      const to = m.to?.[0]?.trim();
      if (from && to) {
        valueMapping[from] = to;
      }
    });
    console.log("updateTargetValuesAndMapping valueMapping", valueMapping);
    const newResponse = cloneDeep(responseMapping);
    set(newResponse, `[${index}].targetValues`, targetValues);
    set(newResponse, `[${index}].valueMapping`, valueMapping);
    set(newResponse, `[${index}].groupId`, groupId);
    setResponseMapping(newResponse);
  };
    
  const handleChangeFrom = (value: string, key: React.Key) => {
    console.log("handleChangeFrom value:", value);
    const cloneArr = cloneDeep(listMapping);
    const idx = cloneArr.findIndex((l) => l.key === key);
    set(cloneArr, `[${idx}].from`, value);
    setListMappingStateResponse(cloneArr);
    updateTargetValuesAndMapping(cloneArr);
  };
  
  const handleChangeTo = (value: string, key: React.Key) => {
    console.log("handleChangeTo value:", value);
    const cloneArr = cloneDeep(listMapping);
    const idx = cloneArr.findIndex((l) => l.key === key);
    set(cloneArr, `[${idx}].to`, [value]);
    setListMappingStateResponse(cloneArr);
    updateTargetValuesAndMapping(cloneArr);
  };

  console.log("ResponseItem item title", item.title);
  console.log("ResponseItem item targetValues", item.targetValues);
  console.log("isEmpty(item?.targetValues)", isEmpty(item?.targetValues))
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
      <Flex className={styles.container} gap={8} wrap="wrap" align="flex-start">
        {/* Sonata side Target response input */}
        <TargetInput
          item={item}
          index={index}
          onChange={updateResponseMapping}
        />

        <span className={styles.mappingIcon}>
          <MappingIcon />
        </span>

        {/* Seller side Source response input */}
        <SourceInput item={item} index={index} />
      </Flex>
      {/* Case-1. When targetValues is not empty */}
      {!isEmpty(item?.targetValues) && (
        <Flex vertical gap={20} style={{ width: "100%", marginTop: 8 }}>
          {listMapping
            ?.filter((i) => {
              if (item.groupId) {
                return i.groupId === item.groupId;
              }
              return i.name === item?.name;
            }
          )
            ?.map(({ key, to, from}) => { 
              if (!item.customizedField) {
                return (
                  <Flex
                    className={styles.itemContainer}
                    key={`${item.title}-${item.name}-${key}`}
                    gap={8}
                    wrap="wrap"
                    align="center"
                  >
                    <Flex align="center" gap={8} style={{ flex: 1 }}>
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
                      <Button
                        className={styles.btnRemoveValueMapping}
                         data-testid="btn-resp-delete-mapping-items"
                        type="link"
                        onClick={() => handleDeleteMapping(key)}
                      >
                        <DeleteOutlined />
                      </Button>
                    </Flex>
                    <MappingIcon />
                    <Select
                      popupClassName={styles.selectPopup}
                      mode="tags"
                      key={`enum-${key}`}
                      placeholder="Input seller order state, Enter for multiple states"
                      style={{ flex: 1 }}
                      value={to}
                      className={styles.stateSelect}
                      onChange={(e) => handleChangeInput(e, key)}
                    />
                  </Flex>
                );
              } else {
                return (
                  <Flex
                    className={styles.itemContainer}
                    align="center"
                    key={`${item.title}-${item.name}-${key}`}
                    wrap="wrap"
                    gap={8}
                  >
                   <Input
                      placeholder="To value"
                      value={to?.[0] ?? ""}
                      onChange={(e) => handleChangeTo(e.target.value, key)}
                      style={{ flex: 1 }}
                    />
                    <MappingIcon />
                    <Button
                      className={styles.btnRemoveValueMapping}
                      type="link"
                      onClick={() => handleDeleteMappingCustomized(key)}
                    >
                      <DeleteOutlined />
                    </Button>
                    <Input
                      placeholder="From value"
                      value={from ?? ""}
                      onChange={(e) => handleChangeFrom(e.target.value, key)}
                      style={{ flex: 1 }}
                    />
                  </Flex>
                );}
            })}
          <Flex className={styles.itemContainer}>
            <Button
              type="link"
              onClick={() => handleAdd(item?.name)}
              data-testid="btn-add-state"
            >
              + Add value mapping
            </Button>
          </Flex>
        </Flex>
      )}
      {/* Case-2. When targetValues is empty */}
      {isEmpty(item?.targetValues) && item.customizedField && (
        <Flex vertical gap={20} style={{ marginTop: 8, width: "100%" }}>
          {listMapping
            ?.filter((i) => {
              if (item.groupId) {
                return i.groupId === item.groupId;
              }
              return i.name === item?.name;
            }
          )
            ?.map(({ key, to, from}) => { 
              console.log("item?.name", item?.name);
              return (
              <Flex
                className={styles.itemContainer}
                align="center"
                key={`${item.title}-${item.name}-${key}`}
                wrap="wrap"
                gap={8}
              >
               <Input
                  placeholder="To value"
                  value={to?.[0] ?? ""}
                  onChange={(e) => handleChangeTo(e.target.value, key)}
                  style={{ flex: 1 }}
                /> 
                <MappingIcon />
                <Button
                  className={styles.btnRemoveValueMapping}
                  type="link"
                  onClick={() => handleDeleteMappingCustomized(key)}
                >
                  <DeleteOutlined />
                </Button>
                <Input
                  placeholder="From value"
                  value={from ?? ""}
                  onChange={(e) => handleChangeFrom(e.target.value, key)}
                  style={{ flex: 1 }}
                />
              </Flex>
            );}
            )}

          <Flex className={styles.itemContainer}>
            <Button
              style={{ marginBottom: 12 }}
              type="link"
              onClick={() => handleAdd(item?.name)}
            >
              + Add value mapping
            </Button>
          </Flex>
        </Flex>
      )}
    </div>
  );
};

export default ResponseItem;
