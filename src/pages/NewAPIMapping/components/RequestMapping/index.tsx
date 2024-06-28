import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import LogMethodTag from "@/components/LogMethodTag";
import Text from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { RightOutlined } from "@ant-design/icons";
import { Collapse, CollapseProps, Flex, Tag, Typography } from "antd";
import { capitalize, groupBy } from "lodash";
import SonataPropMapping from "../SonataPropMapping";
import styles from "./index.module.scss";
import clsx from "clsx";
import { useEffect, useMemo, useState } from "react";
import { IRequestMapping } from "@/utils/types/component.type";

const RequestMapping = () => {
  const { query, sellerApi, rightSide, setRightSide, requestMapping } =
    useNewApiMappingStore();
  const queryData = JSON.parse(query ?? "{}");
  const items: CollapseProps["items"] = useMemo(() => {
    if (requestMapping.length === 0) {
      return [
        {
          label: <Text.NormalLarge>Property mapping</Text.NormalLarge>,
          key: "Property mapping",
          children: <SonataPropMapping list={[]} title="Property mapping" />,
        },
      ];
    }
    const requestMappingGroupedByTitle = groupBy(
      requestMapping,
      (request) => request.title
    );
    return Object.entries(requestMappingGroupedByTitle).map(
      ([title, listMapping]) => ({
        key: title,
        label: <Text.NormalLarge>{title}</Text.NormalLarge>,
        children: (
          <SonataPropMapping
            list={listMapping as IRequestMapping[]}
            title={title}
          />
        ),
      })
    );
  }, [requestMapping]);
  const [activeKey, setActiveKey] = useState<string[]>([]);

  const handleChangeKey = (key: string | string[]) => {
    if (typeof key === "string") {
      setActiveKey([key]);
      return;
    }
    setActiveKey(key);
  };
  useEffect(() => {
    if (requestMapping.length === 0) {
      setActiveKey(["Property mapping"]);
      return;
    }
    setActiveKey(requestMapping.map((rm) => rm.title));
  }, [requestMapping]);
  return (
    <>
      <Flex gap={60}>
        <Flex
          align="center"
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
        <Text.NormalLarge
          style={{
            boxSizing: "border-box",
            flex: "0 0 calc(50% - 30px)",
            padding: 10,
          }}
        >
          Seller API
        </Text.NormalLarge>
      </Flex>
      <Flex align="center" gap={9}>
        <Flex
          align="center"
          gap={6}
          className={styles.sonataAPIBasicInfoWrapper}
        >
          <LogMethodTag method={queryData?.method} />
          <Typography.Text
            style={{ flex: 1, color: "#595959" }}
            ellipsis={{ tooltip: true }}
          >
            {queryData?.path}
          </Typography.Text>
        </Flex>
        <div style={{ flex: "0 0 42px", width: 42 }}>
          <MappingIcon />
        </div>
        <Flex
          align="center"
          justify="space-between"
          className={clsx(styles.sellerAPIBasicInfoWrapper, {
            [styles.highlight]: rightSide === EnumRightType.SelectSellerAPI,
          })}
          onClick={() => {
            setRightSide(EnumRightType.SelectSellerAPI);
          }}
        >
          <Flex align="center" gap={12} style={{ width: "100%" }}>
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
      <Collapse
        ghost
        items={items}
        activeKey={activeKey}
        onChange={handleChangeKey}
        className={styles.collapse}
      />
    </>
  );
};

export default RequestMapping;
