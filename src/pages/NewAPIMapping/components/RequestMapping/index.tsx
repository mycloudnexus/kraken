import Text from "@/components/Text";
import { useGetComponentList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { Collapse, Flex, Tag } from "antd";
import { capitalize } from "lodash";
import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import styles from "./index.module.scss";
import LogMethodTag from "@/components/LogMethodTag";
import { RightOutlined } from "@ant-design/icons";

const RequestMapping = () => {
  const { currentProduct } = useAppStore();
  const { query } = useNewApiMappingStore();
  const queryData = JSON.parse(query ?? '{}');
  const { data: mappingFile } =
    useGetComponentList(currentProduct, {
      kind: "kraken.component.api-target",
      q: query,
      facetIncluded: true,
      page: 0,
      size: 20,
    });
  const { data: sellerAPIList } =
    useGetComponentList(currentProduct, {
      kind: "kraken.component.api-target-spec",
      facetIncluded: true,
      page: 0,
      size: 20,
    });
  console.log(mappingFile, sellerAPIList, queryData);
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
          style={{ flex: "0 0 calc(50% - 30px)" }}
          className={styles.sonataAPIBasicInfoWrapper}
        >
          <LogMethodTag method={queryData?.method?.toUpperCase()} />
          <Text.NormalMedium style={{ color: "#595959" }}>
            {queryData?.path}
          </Text.NormalMedium>
        </Flex>
        <div style={{ width: 42 }}>
          <MappingIcon />
        </div>
        <Flex
          align="center"
          justify="space-between"
          style={{ flex: "0 0 calc(50% - 30px)" }}
          className={styles.sellerAPIBasicInfoWrapper}
        >
          <Flex align="center" gap={12}>
            <LogMethodTag method="GET" />
            <Text.NormalMedium>/api/pricing/calculate</Text.NormalMedium>
          </Flex>
          <RightOutlined style={{ color: "rgba(0, 0, 0, 0.45)" }} />
        </Flex>
        <Collapse />
      </Flex>
    </>
  );
};

export default RequestMapping;
