import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import LogMethodTag from "@/components/LogMethodTag";
import Text from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { RightOutlined } from "@ant-design/icons";
import { Collapse, CollapseProps, Flex, Tag, Typography } from "antd";
import { capitalize } from "lodash";
import SonataPropMapping from "../SonataPropMapping";
import styles from "./index.module.scss";

interface Props {
  openRight?: (value: EnumRightType) => void;
}
const RequestMapping = ({ openRight }: Readonly<Props>) => {
  const { query } = useNewApiMappingStore();
  const queryData = JSON.parse(query ?? "{}");
  const items: CollapseProps["items"] = [
    {
      key: "1",
      label: (
        <>
          <div>
            <Text.NormalLarge>Property mapping</Text.NormalLarge>
          </div>
          <div>
            <Text.NormalMedium color="rgba(0, 0, 0, 0.45)">
              Please map the following Sonata API response properties with
              Seller API response
            </Text.NormalMedium>
          </div>
        </>
      ),
      children: <SonataPropMapping openRight={openRight} />,
    },
  ];
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
          style={{ flex: "0 0 calc(50% - 30px)", width: "calc(50% - 30px)" }}
          className={styles.sonataAPIBasicInfoWrapper}
        >
          <LogMethodTag method={queryData?.method?.toUpperCase()} />
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
          style={{ flex: "0 0 calc(50% - 30px)", width: "calc(50% - 30px)" }}
          className={styles.sellerAPIBasicInfoWrapper}
        >
          <Flex align="center" gap={12}>
            <LogMethodTag method="GET" />
            <Typography.Text style={{ flex: 1 }} ellipsis={{ tooltip: true }}>
              /api/pricing/calculate
            </Typography.Text>
          </Flex>
          <RightOutlined style={{ color: "rgba(0, 0, 0, 0.45)" }} />
        </Flex>
      </Flex>
      <Collapse ghost items={items} className={styles.collapse} />
    </>
  );
};

export default RequestMapping;
