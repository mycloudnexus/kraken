import LogMethodTag from "@/components/LogMethodTag";
import Text from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { RightOutlined } from "@ant-design/icons";
import { Flex, Tag, Tooltip, Typography } from "antd";
import clsx from "clsx";
import { capitalize } from "lodash";
import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import styles from "./index.module.scss";
const HeaderMapping = () => {
  const { query, sellerApi, rightSide, setRightSide } = useNewApiMappingStore();
  const queryData = JSON.parse(query ?? "{}");
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
          <Flex>
            {queryData?.productType && (
              <Tooltip title="Product type">
                <Tag>{queryData.productType.toUpperCase()}</Tag>
              </Tooltip>
            )}
            {queryData?.actionType && (
              <Tooltip title="Action type">
                <Tag>{capitalize(queryData.actionType)}</Tag>
              </Tooltip>
            )}
          </Flex>
        </Flex>
        <Text.NormalLarge
          style={{
            boxSizing: "border-box",
            flex: "0 0 calc(50% - 30px)",
            padding: "10px 5.5px",
          }}
        >
          Seller API
        </Text.NormalLarge>
      </Flex>
      <Flex align="center" gap={9} style={{ marginBottom: 26 }}>
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
    </>
  );
};

export default HeaderMapping;
