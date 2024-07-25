import { IResponseMapping } from "@/utils/types/component.type";
import styles from "./index.module.scss";
import { Button, Flex, Input, Select } from "antd";
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
import { useMappingUiStore } from "@/stores/mappingUi.store";
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
  const { setMappingInProgress } = useMappingUiStore();

  const onChangeTitle = () => {
    const newResponse = cloneDeep(responseMapping);
    set(newResponse, `[${index}].title`, titleInput);
    setResponseMapping(newResponse);
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
    const newResponse = cloneDeep(responseMapping);
    set(newResponse, `[${index}].description`, descriptionInput);
    setResponseMapping(newResponse);
    disableEditDescription();
  };

  const openSelectorForProp = (name?: string, target?: string) => {
    setActiveResponseName(`${name}-${target}`);
    setRightSide(EnumRightType.AddSellerResponse);
    setMappingInProgress(true);
  };

  const handleChangeResponse = (
    value: string,
    name: string,
    target: string
  ) => {
    const cloneObj = cloneDeep(responseMapping);
    const index = cloneObj?.findIndex(
      (i) => i.name === name && i.target === target
    );
    set(cloneObj, `[${index}].source`, value);
    set(cloneObj, `[${index}].sourceLocation`, `BODY`);
    setResponseMapping(cloneObj);
    setActiveResponseName(undefined);
    setMappingInProgress(true);
  };

  const handleSetListMapping = (array: Array<any>) => {
    setMappingInProgress(true);
    setListMappingStateResponse(array);
  };

  const handleAdd = (name: string) => {
    handleSetListMapping([
      ...listMapping,
      {
        key: get(listMapping, `[${listMapping.length - 1}].key`, 0) + 1,
        from: undefined,
        to: undefined,
        name,
      },
    ]);
  };

  const handleDelete = (key: number) => {
    handleSetListMapping(listMapping.filter((item) => item.key !== key));
  };

  const handleSelect = (v: string, key: number) => {
    const cloneArr = cloneDeep(listMapping);
    const index = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${index}].from`, v);
    handleSetListMapping(cloneArr);
  };

  const handleChangeInput = (v: string[], key: number) => {
    const cloneArr = cloneDeep(listMapping);
    const index = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${index}].to`, v);
    handleSetListMapping(cloneArr);
  };

  return (
    <div className={styles.root}>
      <Flex vertical gap={4} className={styles.header}>
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
            <Text.NormalMedium>
              {item.title}
              {item?.requiredMapping && (
                <span style={{ color: "#FF4D4F" }}> *</span>
              )}
            </Text.NormalMedium>
            {!item?.requiredMapping && (
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
            {!item?.requiredMapping && (
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
        <div className={styles.target}>
          <Text.LightMedium lineHeight="32px">
            {!isEmpty(item?.target)
              ? item?.target
              : "No mapping to Sonata API is required"}
          </Text.LightMedium>
        </div>
        <MappingIcon />
        <Input
          variant="filled"
          placeholder="Select or input property"
          style={{
            width: "calc(50% - 30px)",
          }}
          className={clsx(
            styles.input,
            activeResponseName === `${item?.name}-${item?.target}` &&
              styles.activeInput
          )}
          value={isEmpty(item?.source) ? undefined : get(item, "source")}
          onClick={() => {
            setRightSide(EnumRightType.AddSellerResponse);
            openSelectorForProp(item?.name, get(item, "target"));
          }}
          onChange={(e) =>
            handleChangeResponse(e.target.value, item?.name, item?.target)
          }
          suffix={<RightOutlined style={{ fontSize: 12, color: "#C9CDD4" }} />}
        />
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
                  <DeleteOutlined onClick={() => handleDelete(key)} />
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
              + {item.title}
            </Button>
          </Flex>
        </Flex>
      )}
    </div>
  );
};

export default ResponseItem;
