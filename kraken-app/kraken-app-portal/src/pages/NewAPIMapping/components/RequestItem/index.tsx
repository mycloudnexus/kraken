import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import { Text } from "@/components/Text";
import { Input } from "@/components/form";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { IRequestMapping } from "@/utils/types/component.type";
import {
  CheckOutlined,
  CloseOutlined,
  DeleteOutlined,
  EditOutlined,
  DownOutlined,
} from "@ant-design/icons";
import { Button, Dropdown, Flex, Select } from "antd";
import { ItemType } from "antd/es/menu/interface";
import clsx from "clsx";
import { cloneDeep, difference, isEmpty, set } from "lodash";
import { nanoid } from "nanoid";
import { Dispatch, SetStateAction, useEffect, useState } from "react";
import { useBoolean } from "usehooks-ts";
import { SourceInput } from "./SourceInput";
import { TargetInput } from "./TargetInput";
import { handleDeleteMappingItems } from "@/pages/NewAPIMapping/helper";
import styles from "./index.module.scss";

type Props = {
  item: IRequestMapping;
  index: number;
};

const menuItems: ItemType[] = [
  {
    label: "Continuous values",
    key: "continuous",
  },
  {
    label: "Discrete values",
    key: "discrete",
  },
];

const extractSourceValueString = (
  sourceValues: Array<number | string>,
  discrete: boolean | undefined,
  part?: string
) => {
  if (discrete) {
    return sourceValues.join();
  } else if (part === "from") {
    return sourceValues[0].toString();
  } else if (part === "to") {
    return sourceValues[sourceValues.length - 1].toString();
  } else {
    return "";
  }
};

const LimitTypeDropdown = ({
  limitRangeType,
  setLimitRangeType,
  onChangeLimitType,
}: {
  limitRangeType: string;
  setLimitRangeType: Dispatch<SetStateAction<string>>;
  onChangeLimitType: (key: string) => void;
}) => (
  <Dropdown
    trigger={["click"]}
    menu={{
      items: menuItems,
      selectable: true,
      selectedKeys: [limitRangeType],
      onClick: (e) => {
        setLimitRangeType(e.key);
        onChangeLimitType(e.key);
      },
    }}
  >
    <Button
      className={styles.discreteSelector}
      type="link"
      icon={<DownOutlined />}
      iconPosition="end"
    >
      {limitRangeType === "discrete"
        ? 'Discrete values (Use "," to separate if multiple values entered)'
        : "Continuous values"}
    </Button>
  </Dropdown>
);

const RequestItem = ({ item, index }: Props) => {
  const {
    requestMapping,
    setRequestMapping,
    listMappingStateRequest: listMapping,
    setListMappingStateRequest,
  } = useNewApiMappingStore();
  const [titleInput, setTitleInput] = useState("");
  const [descriptionInput, setDescriptionInput] = useState("");
  const [editValueLimit, setEditValueLimit] = useState(false);
  const [limitRangeType, setLimitRangeType] = useState(
    item?.discrete ? "discrete" : "continuous"
  );
  const [continuousInput, setContinuousInput] = useState(
    item.allowValueLimit && !item.discrete
      ? {
          from: item.sourceValues?.[0] ?? 0,
          to: item.sourceValues?.[1] ?? 0,
        }
      : {
          from: 0,
          to: 0,
        }
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

  const onChangeLimitType = (key: string) => {
    const newRequest = cloneDeep(requestMapping);
    set(newRequest, `[${index}].discrete`, key === "discrete");
    setRequestMapping(newRequest);
  };

  const handleDelete = () => {
    const newRequest = cloneDeep(requestMapping);
    newRequest.splice(index, 1);
    setRequestMapping(newRequest);
  };

  const handleAdd = (name: string) => {
    setListMappingStateRequest([
      ...listMapping,
      {
        key: nanoid(),
        from: undefined,
        to: undefined,
        name,
      },
    ]);
  };

  const handleDeleteMapping = (key: React.Key) => {
    const updated = handleDeleteMappingItems(key, listMapping, undefined);
    setListMappingStateRequest(updated);
  };

  const handleSelect = (value: string, key: React.Key) => {
    const cloneArr = cloneDeep(listMapping);
    const itemIndex = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${itemIndex}].from`, value);
    setListMappingStateRequest(cloneArr);
  };

  const handleChangeInput = (value: string[], key: React.Key) => {
    const cloneArr = cloneDeep(listMapping);
    const itemIndex = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${itemIndex}].to`, value);
    setListMappingStateRequest(cloneArr);
  };

  const handleChangeInputContinuousFrom = (value: string) => {
    setContinuousInput({ ...continuousInput, from: value });
  };

  const handleChangeInputContinuousTo = (value: string) => {
    setContinuousInput({ ...continuousInput, to: value });
  };

  const handleChangeInputDiscrete = (value: string) => {
    const newRequest = cloneDeep(requestMapping);
    const discreteArray = value.split(",").map((item) => item.trim());
    set(newRequest, `[${index}].sourceValues`, discreteArray);
    set(newRequest, `[${index}].discrete`, true);
    setRequestMapping(newRequest);
  };

  const handleDeleteLimit = () => {
    const newRequest = cloneDeep(requestMapping);
    set(newRequest, `[${index}].sourceValues`, undefined);
    setRequestMapping(newRequest);
  };

  useEffect(() => {
    const continuousInputValues = Object.values(continuousInput);
    if (continuousInputValues[1] > continuousInputValues[0]) {
      const newRequest = cloneDeep(requestMapping);
      set(
        newRequest,
        `[${index}].sourceValues`,
        Object.values(continuousInput)
      );
      set(newRequest, `[${index}].discrete`, false);
      setRequestMapping(newRequest);
    }
  }, [continuousInput]);

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
                onChange={(value) => setTitleInput(value)}
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
              onChange={(value) => setDescriptionInput(value)}
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
      <Flex className={styles.container} gap={8} wrap="wrap" align="flex-start">
        {/* Source property mapping */}
        <SourceInput item={item} index={index} />

        <span className={styles.mappingIcon}>
          <MappingIcon />
        </span>

        {/* Target property mapping */}
        <TargetInput item={item} index={index} />
      </Flex>
      {!isEmpty(item?.sourceValues) && !item.allowValueLimit && (
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
                    data-testid="btn-req-delete-mapping-items"
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
                  placeholder="Input seller order state"
                  value={to?.[0]}
                  style={{ flex: 1 }}
                  className={styles.stateSelect}
                  onChange={(value) => handleChangeInput([value], key)}
                />
              </Flex>
            ))}
          <Flex className={styles.itemContainer}>
            <Button
              style={{ marginBottom: 12 }}
              data-testid="btn-add-state"
              type="link"
              onClick={() => handleAdd(item?.name)}
            >
              + Add value mapping
            </Button>
          </Flex>
        </Flex>
      )}
      {item.allowValueLimit && isEmpty(item?.sourceValues) && (
        <Flex className={styles.limitRangeContainer}>
          {editValueLimit ? (
            <div>
              <LimitTypeDropdown
                limitRangeType={limitRangeType}
                setLimitRangeType={setLimitRangeType}
                onChangeLimitType={onChangeLimitType}
              />
              <Flex style={{ marginBottom: "12px" }}>
                {limitRangeType === "discrete" ? (
                  <div>
                    <Input
                      onChange={(value) => handleChangeInputDiscrete(value)}
                    />
                  </div>
                ) : (
                  <Flex>
                    <Input
                      onChange={(value) =>
                        handleChangeInputContinuousFrom(value)
                      }
                    />
                    <span style={{ margin: "0 5px" }}>To</span>
                    <Input
                      onChange={(value) => handleChangeInputContinuousTo(value)}
                    />
                  </Flex>
                )}
                <Button
                  className={styles.btnRemoveValueMapping}
                  type="link"
                  onClick={() => setEditValueLimit(false)}
                >
                  <DeleteOutlined />
                </Button>
              </Flex>
            </div>
          ) : (
            <Button
              style={{ marginBottom: 12 }}
              data-testid="btn-add-valuelimit-int"
              type="link"
              onClick={() => setEditValueLimit(true)}
            >
              Add value limit
            </Button>
          )}
        </Flex>
      )}
      {item.allowValueLimit && !isEmpty(item?.sourceValues) && (
        <Flex className={styles.limitRangeContainer}>
          <div>
            <LimitTypeDropdown
              limitRangeType={limitRangeType}
              setLimitRangeType={setLimitRangeType}
              onChangeLimitType={onChangeLimitType}
            />
            <Flex style={{ marginBottom: "12px" }}>
              {limitRangeType === "discrete" ? (
                <div>
                  <Input
                    value={extractSourceValueString(
                      item.sourceValues || [0, 0],
                      item.discrete
                    )}
                    onChange={(value) => handleChangeInputDiscrete(value)}
                  />
                </div>
              ) : (
                <Flex>
                  <Input
                    onChange={(value) => handleChangeInputContinuousFrom(value)}
                    value={extractSourceValueString(
                      item.sourceValues || [0, 0],
                      item.discrete,
                      "from"
                    )}
                  />
                  <span style={{ margin: "0 5px" }}>To</span>
                  <Input
                    onChange={(value) => handleChangeInputContinuousTo(value)}
                    value={extractSourceValueString(
                      item.sourceValues || [0, 0],
                      item.discrete,
                      "to"
                    )}
                  />
                </Flex>
              )}
              <Button
                className={styles.btnRemoveValueMapping}
                type="link"
                onClick={handleDeleteLimit}
              >
                <DeleteOutlined />
              </Button>
            </Flex>
          </div>
        </Flex>
      )}
    </div>
  );
};

export default RequestItem;
