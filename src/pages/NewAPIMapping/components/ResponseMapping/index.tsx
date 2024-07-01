import MappingIcon from "@/assets/newAPIMapping/mapping-icon-response.svg";
import ExpandCard from "@/components/ExpandCard";
import Flex from "@/components/Flex";
import Text from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { DeleteOutlined, RightOutlined } from "@ant-design/icons";
import { Button, Input, Select, Tag, Typography } from "antd";
import clsx from "clsx";
import {
  capitalize,
  chain,
  cloneDeep,
  difference,
  fromPairs,
  get,
  groupBy,
  isEmpty,
  set,
} from "lodash";
import React, { Fragment, useEffect, useMemo, useState } from "react";
import styles from "./index.module.scss";
import LogMethodTag from "@/components/LogMethodTag";

interface IMapping {
  key: number;
  from?: string;
  to?: string;
  name?: string;
}

const buildInitListMapping = (responseMapping: any[]) => {
  let k = 1;
  const list: IMapping[] = [];
  responseMapping.forEach((item) => {
    Object.entries((item.valueMapping ?? {}) as Record<string, string>).map(
      ([to, from]) => {
        const mapItem = {
          key: k,
          from,
          to,
          name: item.name,
        };
        list.push(mapItem);
        k++;
      }
    );
  });
  return list;
};

interface MappingCollapseProps {
  title: string;
  items: any[];
  listMapping: IMapping[];
  handleSelect: (value: string, key: number) => void;
  handleAdd: (value: string) => void;
  handleDelete: (key: number) => void;
  handleChangeInput: (value: string, key: number) => void;
  openSelectorForProp: (value?: string) => void;
  handleChangeResponse: (value: string, index: number) => void;
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
}: Readonly<MappingCollapseProps>) => (
  <div style={{ marginTop: 26 }} key={`main-${title}`}>
    <ExpandCard title={title} defaultValue description={""}>
      <Flex gap={16} justifyContent="flex-start" alignItems="stretch">
        <Flex
          className={clsx(["flex-1", styles.mainCard])}
          flexDirection="column"
          alignItems="flex-start"
          justifyContent="flex-start"
          gap={4}
        >
          <Text.BoldMedium>Property from Sonata API response</Text.BoldMedium>
          {items.map((item) => (
            <Fragment key={item.target}>
              <Text.LightMedium lineHeight="32px">
                {!isEmpty(item?.target)
                  ? item?.target
                  : "No mapping to Sonata API is required"}
              </Text.LightMedium>
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
                        <React.Fragment key={`state-${key}`}>
                          <Text.BoldMedium>Sonata state</Text.BoldMedium>
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
          style={{ marginTop: 57 }}
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
                <Flex flexDirection="column" gap={59} style={{ marginTop: 48 }}>
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
          <Text.BoldMedium>Property from Seller API response</Text.BoldMedium>
          {items.map((item, index) => (
            <Fragment key={item.target}>
              <Input
                placeholder="Select response property"
                style={{ width: "100%" }}
                className={styles.input}
                value={isEmpty(item?.source) ? undefined : get(item, "source")}
                onClick={() => {
                  openSelectorForProp(item?.name);
                }}
                onChange={(e) => handleChangeResponse(e.target.value, index)}
                suffix={
                  <RightOutlined style={{ fontSize: 12, color: "#C9CDD4" }} />
                }
              />
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
                        <React.Fragment key={`enum-${key}`}>
                          <Text.BoldMedium>Seller state</Text.BoldMedium>
                          <Input
                            placeholder="Input seller state"
                            style={{ width: "100%" }}
                            value={to}
                            className={styles.input}
                            onChange={(e) =>
                              handleChangeInput(e.target.value, key)
                            }
                          />
                        </React.Fragment>
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
    query,
    responseMapping,
    rightSide,
    sellerApi,
    setActiveResponseName,
    setResponseMapping,
    setRightSide,
  } = useNewApiMappingStore();
  const queryData = JSON.parse(query ?? "{}");
  const [listMapping, setListMapping] = useState<IMapping[]>(
    buildInitListMapping(responseMapping)
  );

  const handleSelect = (v: string, key: number) => {
    const cloneArr = cloneDeep(listMapping);
    const index = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${index}].from`, v);
    setListMapping(cloneArr);
  };

  useEffect(() => {
    const newData = chain(listMapping)
      .groupBy("name")
      .map((items, name) => ({
        name,
        valueMapping: fromPairs(items.map((item) => [item.to, item.from])),
      }))
      .value();
    if (isEmpty(newData)) {
      return;
    }
    const newResponse = cloneDeep(responseMapping);
    for (const n of newData) {
      const index = newResponse?.findIndex((i: any) => (i.name = n.name));
      set(newResponse, `[${index}].valueMapping`, n.valueMapping);
    }
    setResponseMapping(newResponse);
  }, [listMapping]);

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

  const handleChangeInput = (v: string, key: number) => {
    const cloneArr = cloneDeep(listMapping);
    const index = listMapping.findIndex((l) => l.key === key);
    set(cloneArr, `[${index}].to`, v);
    setListMapping(cloneArr);
  };

  const openSelectorForProp = (name?: string) => {
    setActiveResponseName(name);
    setRightSide(EnumRightType.AddSellerResponse);
  };

  const handleChangeResponse = (value: string, index: number) => {
    const cloneObj = cloneDeep(responseMapping);

    set(cloneObj, `[${index}].source`, value);
    set(cloneObj, `[${index}].sourceLocation`, `BODY`);
    setResponseMapping(cloneObj);
    setActiveResponseName(undefined);
  };

  useEffect(() => {
    if (isEmpty(sellerApi)) {
      return;
    }
    setRightSide(EnumRightType.AddSellerResponse);
  }, [sellerApi]);

  useEffect(() => {
    if (isEmpty(responseMapping)) {
      setListMapping([]);
    }
  }, [responseMapping]);

  const responseMappingGroupedByTitle = useMemo(() => {
    return groupBy(responseMapping, "title");
  }, [responseMapping]);

  return (
    <div className={styles.root}>
      <Flex gap={16} justifyContent="flex-start">
        <Flex
          justifyContent="flex-start"
          gap={8}
          style={{
            boxSizing: "border-box",
            flex: "0 0 calc(50% - 30px)",
            padding: 10,
          }}
        >
          <Text.NormalLarge>Sonata API</Text.NormalLarge>
          {queryData?.productType && (
            <Tag>{queryData.productType.toUpperCase()}</Tag>
          )}
          {queryData?.actionType && (
            <Tag>{capitalize(queryData.actionType)}</Tag>
          )}
        </Flex>
        <div style={{ visibility: "hidden" }}>
          <MappingIcon />
        </div>
        <div className="flex-1">
          <Text.NormalLarge>Seller API</Text.NormalLarge>
        </div>
      </Flex>
      <Flex gap={16} justifyContent="flex-start" style={{ marginTop: 16 }}>
        <Flex gap={6} className={styles.sonataAPIBasicInfoWrapper}>
          <LogMethodTag method={queryData?.method} />
          <Typography.Text
            style={{ flex: 1, color: "#595959" }}
            ellipsis={{ tooltip: true }}
          >
            {queryData?.path}
          </Typography.Text>
        </Flex>
        <MappingIcon />
        <Flex
          justifyContent="space-between"
          className={clsx(styles.sellerAPIBasicInfoWrapper, {
            [styles.highlight]: rightSide === EnumRightType.SelectSellerAPI,
          })}
          onClick={() => {
            setRightSide(EnumRightType.SelectSellerAPI);
          }}
        >
          <Flex gap={12} style={{ width: "100%" }}>
            {sellerApi ? (
              <>
                <LogMethodTag method={sellerApi.method} />
                <Typography.Text
                  style={{ flex: 1 }}
                  ellipsis={{ tooltip: true }}
                >
                  {sellerApi.url}
                </Typography.Text>
              </>
            ) : (
              <Typography.Text>
                Please select API from the side bar
              </Typography.Text>
            )}
          </Flex>
          <RightOutlined style={{ color: "rgba(0, 0, 0, 0.45)" }} />
        </Flex>
      </Flex>
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
        />
      ))}
    </div>
  );
};

export default ResponseMapping;
