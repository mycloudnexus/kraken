import LogMethodTag from "@/components/LogMethodTag";
import { Text } from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { CloseCircleFilled, RightOutlined } from "@ant-design/icons";
import { Flex, Modal, Typography } from "antd";
import clsx from "clsx";
import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import MappingReverseIcon from "@/assets/newAPIMapping/mapping-icon-reverse.svg";
import styles from "./index.module.scss";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import { get, isEmpty } from "lodash";
import { useBoolean } from "usehooks-ts";
import {
  IRequestMapping,
  IResponseMapping,
} from "@/utils/types/component.type";

type Props = {
  disabled: boolean;
  isFocus: boolean;
  handleClick: () => void;
};
const SellerAPI = ({ disabled, isFocus, handleClick }: Props) => {
  const { sellerApi } = useNewApiMappingStore();
  if (disabled) {
    return (
      <Typography.Text style={{ color: "#00000040" }}>
        Not required
      </Typography.Text>
    );
  }
  if (!isEmpty(sellerApi)) {
    return (
      <>
        <LogMethodTag method={sellerApi.method} />
        <Typography.Text style={{ flex: 1 }} ellipsis={{ tooltip: true }}>
          {sellerApi.url}
        </Typography.Text>
        {isFocus && (
          <CloseCircleFilled
            style={{
              color: "#00000040",
              marginLeft: -4,
              marginRight: 4,
            }}
            onClick={(e) => {
              e.preventDefault();
              handleClick();
            }}
          />
        )}
      </>
    );
  }
  return (
    <Typography.Text style={{ color: "#00000040" }}>
      Please select API
    </Typography.Text>
  );
};
const HeaderMapping = ({
  disabled = false,
  mappers,
}: {
  disabled?: boolean;
  mappers: any;
}) => {
  const { query, sellerApi, rightSide, setRightSide } = useNewApiMappingStore();
  const { activeTab } = useMappingUiStore();
  const queryData = JSON.parse(query ?? "{}");
  const { value: isFocus, setTrue, setFalse } = useBoolean(false);
  const {
    setRequestMapping,
    setResponseMapping,
    setSellerApi,
    setServerKey,
    setListMappingStateResponse,
  } = useNewApiMappingStore();
  const resetMappingFnc = () => {
    setRequestMapping(
      mappers.request
        ?.filter((rm: IRequestMapping) => !rm.customizedField)
        ?.map((rm: IRequestMapping) => ({
          ...rm,
          target: "",
          targetLocation: "",
          targetType: "",
          targetValues: [],
        }))
    );
    setResponseMapping(
      mappers.response
        ?.filter((rm: IResponseMapping) => !rm.customizedField)
        ?.map((rm: IResponseMapping) => ({
          ...rm,
          source: "",
          sourceLocation: "",
          valueMapping: {},
        }))
    );
    setListMappingStateResponse(undefined);
    setSellerApi(undefined);
    setServerKey("");
  };
  const handleClick = () => {
    Modal.confirm({
      className: styles.confirm,
      content:
        "Are you sure to remove this API? All the related properties will be removed as well. Continue?",
      okButtonProps: {
        type: "primary",
      },
      cancelText: "Cancel",
      okText: "Yes, continue",
      okType: "danger",
      onOk: () => {
        resetMappingFnc();
      },
    });
  };
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
          <Text.NormalLarge lineHeight="24px">Sonata API</Text.NormalLarge>
        </Flex>
        <Text.NormalLarge
          style={{
            boxSizing: "border-box",
            flex: "0 0 calc(50% - 30px)",
            padding: "10px 5.5px",
            display: "flex",
            gap: 8,
          }}
          lineHeight="24px"
        >
          Seller API
          <Typography.Text
            style={{
              color: "#00000073",
              fontSize: 12,
              maxWidth: `calc(100% - 80px)`,
              lineHeight: "24px",
            }}
            ellipsis={{ tooltip: true }}
          >
            {get(sellerApi, "name", "")}
          </Typography.Text>
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
        <div className={styles.mappingIcon}>
          {activeTab === "request" ? <MappingIcon /> : <MappingReverseIcon />}
        </div>
        <Flex
          align="center"
          justify="space-between"
          className={clsx(styles.sellerAPIBasicInfoWrapper, {
            [styles.highlight]:
              rightSide === EnumRightType.SelectSellerAPI && !disabled,
          })}
          onClick={() => {
            if (disabled) {
              return;
            }
            setRightSide(EnumRightType.SelectSellerAPI);
          }}
          onMouseEnter={() => {
            setTrue();
          }}
          onMouseLeave={() => {
            setFalse();
          }}
        >
          <Flex align="center" gap={8} style={{ width: "100%" }}>
            <SellerAPI
              isFocus={isFocus}
              disabled={disabled}
              handleClick={handleClick}
            />
          </Flex>
          <RightOutlined style={{ color: "rgba(0, 0, 0, 0.45)" }} />
        </Flex>
      </Flex>
    </>
  );
};

export default HeaderMapping;
