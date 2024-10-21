import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import { Text } from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { IRequestMapping } from "@/utils/types/component.type";
import {
  CheckOutlined,
  CloseOutlined,
  DeleteOutlined,
  EditOutlined,
} from "@ant-design/icons";
import { Button, Flex, Input, Select } from "antd";
import clsx from "clsx";
import { cloneDeep, difference, get, isEmpty, set } from "lodash";
import { useEffect, useState } from "react";
import { useBoolean } from "usehooks-ts";
import { SourceInput } from "./SourceInput";
import { TargetInput } from "./TargetInput";
import styles from "./index.module.scss";

type Props = {
  item: IRequestMapping;
  index: number;
};

const RequestItem = ({ item, index }: Props) => {
  const {
    requestMapping,
    setRequestMapping,
    listMappingStateRequest: listMapping,
    setListMappingStateRequest,
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
  }, [
    item.title,
    descriptionInput,
    setTitleInput,
    setDescriptionInput,
    titleInput,
    item.description,
  ]);

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

  const handleAdd = (name: string) => {
    const newKey = get(listMapping, `[${listMapping.length - 1}].key`, 0) + 1;
    setListMappingStateRequest([
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
    setListMappingStateRequest(listMapping.filter((item) => item.key !== key));
  };

  const handleSelect = (value: string, key: number) => {
    const cloneArr = cloneDeep(listMapping);
    const itemIndex = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${itemIndex}].from`, value);
    setListMappingStateRequest(cloneArr);
  };

  const handleChangeInput = (value: string[], key: number) => {
    const cloneArr = cloneDeep(listMapping);
    const itemIndex = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${itemIndex}].to`, value);
    setListMappingStateRequest(cloneArr);
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
                  data-testid="edit-title"
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
      <Flex className={styles.container} gap={8} wrap="wrap" align="flex-end">
        {/* Source property mapping */}
        <SourceInput item={item} index={index} />

        <span className={styles.mappingIcon}>
          <MappingIcon />
        </span>

        {/* Target property mapping */}
        <TargetInput item={item} index={index} isSellerSideProps />
      </Flex>
      {!isEmpty(item?.sourceValues) && (
        <Flex vertical gap={20} style={{ marginTop: 8, width: "100%" }}>
          {listMapping
            ?.filter((i) => i.name === item?.name)
            ?.map(({ key, from, to }) => (
              <Flex
                className={styles.itemContainer}
                align="center"
                key={`${item.title}-${item.name}-${key}`}
                wrap="wrap"
                gap={8}
              >
                <Flex align="center" gap={8} style={{ flex: 1 }}>
                  <Select
                    data-testid="select-sonata-state"
                    className={styles.stateSelect}
                    placeholder="State"
                    onSelect={(v) => handleSelect(v, key)}
                    value={from}
                    style={{ flex: 1 }}
                    options={difference(
                      item?.sourceValues,
                      listMapping
                        ?.filter((l) => l.name === item.name)
                        .map((item) => item.from)
                    ).map((item) => ({
                      value: item,
                      title: item,
                    }))}
                  />
                  <Button
                    className={styles.btnRemoveValueMapping}
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
                  placeholder="Input seller order state, Enter for multiple states"
                  key={`enum-${key}`}
                  value={to}
                  style={{ flex: 1 }}
                  onChange={(e) => handleChangeInput(e, key)}
                  className={styles.stateSelect}
                />
              </Flex>
            ))}
          <Flex className={styles.itemContainer}>
            <Button
              style={{ marginBottom: 12 }}
              onClick={() => handleAdd(item?.name)}
              data-testid="btn-add-state"
              type="primary"
            >
              + Add Value Mapping
            </Button>
          </Flex>
        </Flex>
      )}
    </div>
  );
};

export default RequestItem;
