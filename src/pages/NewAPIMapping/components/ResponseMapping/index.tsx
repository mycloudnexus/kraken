import Text from "@/components/Text";
import styles from "./index.module.scss";
import Flex from "@/components/Flex";
import MappingIcon from "@/assets/newAPIMapping/mapping-icon-response.svg";
import RequestMethod from "@/components/Method";
import ExpandCard from "@/components/ExpandCard";
import clsx from "clsx";
import { Button, Input, Select, Typography } from "antd";
import { DeleteOutlined, RightOutlined } from "@ant-design/icons";
import React, { useEffect, useState } from "react";
import {
  chain,
  cloneDeep,
  difference,
  fromPairs,
  get,
  isEmpty,
  set,
} from "lodash";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";

interface IMapping {
  key: number;
  from?: string;
  to?: string;
  name?: string;
}
const ResponseMapping = () => {
  const {
    setRightSide,
    sellerApi,
    responseMapping,
    setActiveResponseName,
    query,
    setResponseMapping,
  } = useNewApiMappingStore();
  const queryData = JSON.parse(query ?? "{}");
  const [listMapping, setListMapping] = useState<IMapping[]>([]);

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
        valueMapping: fromPairs(items.map((item) => [item.from, item.to])),
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
  useEffect(() => {
    if (isEmpty(sellerApi)) {
      return;
    }
    setRightSide(EnumRightType.AddSellerResponse);
  }, [sellerApi]);

  return (
    <div className={styles.root}>
      <Flex gap={16} justifyContent="flex-start">
        <div className="flex-1">
          <Text.NormalLarge>Sonota API</Text.NormalLarge>
        </div>
        <div style={{ visibility: "hidden" }}>
          <MappingIcon />
        </div>
        <div className="flex-1">
          <Text.NormalLarge>Seller API</Text.NormalLarge>
        </div>
      </Flex>
      <Flex gap={16} justifyContent="flex-start" style={{ marginTop: 16 }}>
        <div className="flex-1 word-break-all">
          <div className={styles.mainCard} style={{ boxSizing: "border-box" }}>
            <RequestMethod method={queryData?.method?.toUpperCase?.()} />
            <Text.LightMedium>{queryData?.path}</Text.LightMedium>
          </div>
        </div>
        <MappingIcon />
        <div className="flex-1 word-break-all">
          <div
            className={styles.mappingCard}
            style={{
              cursor: "pointer",
              borderColor: isEmpty(sellerApi) ? "#165DFF" : "#dde1e5",
            }}
            role="none"
            onClick={() => {
              setRightSide(EnumRightType.SelectSellerAPI);
            }}
          >
            {isEmpty(sellerApi) ? (
              <Flex justifyContent="space-between" style={{ width: "100%" }}>
                <Typography.Text style={{ flex: 1 }}>
                  Please select API from the side bar
                </Typography.Text>
                <RightOutlined style={{ color: "rgba(0, 0, 0, 0.45)" }} />
              </Flex>
            ) : (
              <>
                <RequestMethod method={sellerApi?.method} />
                <Text.LightMedium>{sellerApi?.url}</Text.LightMedium>
              </>
            )}
          </div>
        </div>
      </Flex>
      {responseMapping?.map((item: any) => (
        <div style={{ marginTop: 26 }} key={`main-${item?.name}`}>
          <ExpandCard
            title={item?.title}
            defaultValue
            description={item?.description}
          >
            <Flex gap={16} justifyContent="flex-start" alignItems="stretch">
              <Flex
                className={clsx(["flex-1", styles.mainCard])}
                flexDirection="column"
                alignItems="flex-start"
                justifyContent="flex-start"
                gap={4}
              >
                <Text.BoldMedium>
                  Property from Sonata API response
                </Text.BoldMedium>
                <Text.LightMedium lineHeight="30px">
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
                                ).map((item) => ({ title: item, value: item }))}
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
              </Flex>
              <Flex
                flexDirection="column"
                justifyContent="flex-end"
                gap={58}
                style={{
                  marginBottom:
                    !isEmpty(item?.target) && !isEmpty(item?.targetValues)
                      ? 78
                      : 36,
                }}
              >
                <div style={{ marginBottom: 10 }}>
                  <MappingIcon />
                </div>
                {Array.from(
                  {
                    length: listMapping?.filter((i) => i.name === item.name)
                      .length,
                  },
                  (_, i) => i
                ).map((key) => (
                  <MappingIcon key={`icon-${key}`} />
                ))}
              </Flex>
              <Flex
                className={clsx(["flex-1", styles.mappingCard])}
                flexDirection="column"
                alignItems="flex-start"
                gap={4}
                justifyContent="flex-start"
              >
                <Text.BoldMedium>
                  Property from Seller API response
                </Text.BoldMedium>
                <Select
                  placeholder="Select response property"
                  suffixIcon={<RightOutlined />}
                  style={{ width: "100%" }}
                  className={styles.select}
                  popupClassName={styles.selectPopup}
                  value={
                    isEmpty(item?.source) ? undefined : get(item, "source")
                  }
                  onClick={() => {
                    setActiveResponseName(item?.name);
                  }}
                />
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
              </Flex>
            </Flex>
          </ExpandCard>
        </div>
      ))}
    </div>
  );
};

export default ResponseMapping;
