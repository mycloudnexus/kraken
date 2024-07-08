import MappingIcon from "@/assets/newAPIMapping/mapping-icon-response.svg";
import ExpandCard from "@/components/ExpandCard";
import Flex from "@/components/Flex";
import Text from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { DeleteOutlined, RightOutlined } from "@ant-design/icons";
import { Button, Input, Select } from "antd";
import clsx from "clsx";
import {
  cloneDeep,
  difference,
  every,
  get,
  groupBy,
  isEmpty,
  set,
} from "lodash";
import React, { Fragment, useEffect, useMemo } from "react";
import styles from "./index.module.scss";

export interface IMapping {
  key: number;
  from?: string;
  to?: string[];
  name?: string;
}

interface MappingCollapseProps {
  title: string;
  items: any[];
  listMapping: IMapping[];
  handleSelect: (value: string, key: number) => void;
  handleAdd: (value: string) => void;
  handleDelete: (key: number) => void;
  handleChangeInput: (value: string[], key: number) => void;
  openSelectorForProp: (value?: string) => void;
  handleChangeResponse: (value: string, title: string) => void;
  activeResponseName?: string;
  setRightSide: (value?: EnumRightType) => void;
}

const MappingCollapse = ({
  title,
  items,
  listMapping,
  handleSelect,
  handleAdd,
  handleDelete,
  handleChangeInput,
  openSelectorForProp,
  handleChangeResponse,
  activeResponseName,
  setRightSide,
}: Readonly<MappingCollapseProps>) => (
  <div style={{ marginTop: 12 }}>
    <ExpandCard
      title={title}
      defaultValue
      description={get(items, `[${0}].description`)}
    >
      <Flex gap={16} justifyContent="flex-start" alignItems="stretch">
        <Flex
          className={clsx(["flex-1", styles.mainCard])}
          flexDirection="column"
          alignItems="flex-start"
          justifyContent="flex-start"
          gap={4}
        >
          {items.map((item) => (
            <Fragment key={item.target}>
              <div
                className={clsx(!isEmpty(item?.targetValues) && styles.target)}
              >
                <Text.LightMedium lineHeight="32px">
                  {!isEmpty(item?.target)
                    ? item?.target
                    : "No mapping to Sonata API is required"}
                </Text.LightMedium>
              </div>
              {!isEmpty(item?.targetValues) && (
                <div style={{ marginTop: 18, width: "100%" }}>
                  <Flex
                    style={{ width: "100%" }}
                    flexDirection="column"
                    gap={8}
                    alignItems="flex-start"
                  >
                    {listMapping
                      ?.filter((i) => i.name === item?.name)
                      ?.map(({ key, from }, index) => (
                        <React.Fragment key={`state-${title}-${key}`}>
                          <Flex
                            justifyContent="flex-start"
                            style={{ width: "100%" }}
                            gap={12}
                          >
                            <Select
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
                            {index !== 0 && (
                              <DeleteOutlined
                                onClick={() => handleDelete(key)}
                              />
                            )}
                          </Flex>
                        </React.Fragment>
                      ))}
                  </Flex>
                  <Button
                    type="primary"
                    style={{ marginTop: 16 }}
                    onClick={() => handleAdd(item?.name)}
                    data-testid="btn-add-state"
                  >
                    Add state
                  </Button>
                </div>
              )}
            </Fragment>
          ))}
        </Flex>
        <Flex
          flexDirection="column"
          justifyContent="flex-start"
          gap={25}
          style={{ marginTop: 28 }}
        >
          {Array.from(
            {
              length: items.length,
            },
            (_, i) => i
          ).map((key) => (
            <MappingIcon key={`icon-${key}`} />
          ))}
          {items.map((item) => (
            <>
              {listMapping?.filter((i) => i.name === item?.name).length > 0 ? (
                <Flex flexDirection="column" gap={28} style={{ marginTop: 30 }}>
                  {listMapping
                    ?.filter((i) => i.name === item?.name)
                    .map((key) => (
                      <MappingIcon key={`icon-${key}`} />
                    ))}
                </Flex>
              ) : null}
            </>
          ))}
        </Flex>
        <Flex
          className={clsx(["flex-1", styles.mappingCard])}
          flexDirection="column"
          alignItems="flex-start"
          gap={4}
          justifyContent="flex-start"
        >
          {items.map((item) => (
            <Fragment key={item.target}>
              <div
                className={clsx(!isEmpty(item?.targetValues) && styles.target)}
                style={{ width: "100%" }}
              >
                <Input
                  placeholder="Select response property"
                  style={{ width: "100%" }}
                  className={clsx(
                    styles.input,
                    activeResponseName === item.name && styles.activeInput
                  )}
                  value={
                    isEmpty(item?.source) ? undefined : get(item, "source")
                  }
                  onClick={() => {
                    setRightSide(EnumRightType.AddSellerResponse);
                    openSelectorForProp(item?.name);
                  }}
                  onChange={(e) => handleChangeResponse(e.target.value, title)}
                  suffix={
                    <RightOutlined style={{ fontSize: 12, color: "#C9CDD4" }} />
                  }
                />
              </div>

              {listMapping?.filter((i) => i.name === item.name).length > 0 && (
                <div style={{ marginTop: 18, width: "100%" }}>
                  <Flex
                    style={{ width: "100%" }}
                    flexDirection="column"
                    gap={8}
                    alignItems="flex-start"
                  >
                    {listMapping
                      ?.filter((i) => i.name === item.name)
                      ?.map(({ key, to }) => (
                        <Select
                          popupClassName={styles.selectPopup}
                          mode="tags"
                          key={`enum-${key}`}
                          placeholder="Input seller state"
                          style={{ width: "100%" }}
                          value={to}
                          className={styles.select}
                          onChange={(e) => handleChangeInput(e, key)}
                        />
                      ))}
                  </Flex>
                </div>
              )}
            </Fragment>
          ))}
        </Flex>
      </Flex>
    </ExpandCard>
  </div>
);

const ResponseMapping = () => {
  const {
    responseMapping,
    setActiveResponseName,
    setResponseMapping,
    setRightSide,
    activeResponseName,
    listMappingStateResponse: listMapping,
    setListMappingStateResponse: setListMapping,
  } = useNewApiMappingStore();

  const handleSelect = (v: string, key: number) => {
    const cloneArr = cloneDeep(listMapping);
    const index = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${index}].from`, v);
    setListMapping(cloneArr);
  };

  const handleAdd = (name: string) => {
    setListMapping([
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
    setListMapping(listMapping.filter((item) => item.key !== key));
  };

  const handleChangeInput = (v: string[], key: number) => {
    const cloneArr = cloneDeep(listMapping);
    const index = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${index}].to`, v);
    setListMapping(cloneArr);
  };

  const openSelectorForProp = (name?: string) => {
    setActiveResponseName(name);
    setRightSide(EnumRightType.AddSellerResponse);
  };

  const handleChangeResponse = (value: string, title: string) => {
    const cloneObj = cloneDeep(responseMapping);
    const index = cloneObj?.findIndex((i) => i.title === title);
    set(cloneObj, `[${index}].source`, value);
    set(cloneObj, `[${index}].sourceLocation`, `BODY`);
    setResponseMapping(cloneObj);
    setActiveResponseName(undefined);
  };

  useEffect(() => {
    if (every(responseMapping, (it) => isEmpty(it.valueMapping))) {
      setListMapping([]);
    }
  }, [responseMapping]);

  const responseMappingGroupedByTitle = useMemo(() => {
    return groupBy(responseMapping, "title");
  }, [responseMapping]);

  return (
    <div className={styles.root}>
      {Object.entries(responseMappingGroupedByTitle).map(([title, items]) => (
        <MappingCollapse
          key={`main-${title}`}
          title={title}
          items={items}
          listMapping={listMapping}
          handleSelect={handleSelect}
          handleAdd={handleAdd}
          handleDelete={handleDelete}
          handleChangeInput={handleChangeInput}
          openSelectorForProp={openSelectorForProp}
          handleChangeResponse={handleChangeResponse}
          activeResponseName={activeResponseName}
          setRightSide={setRightSide}
        />
      ))}
    </div>
  );
};

export default ResponseMapping;
