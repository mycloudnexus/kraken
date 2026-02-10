import MappingReverseIcon from "@/assets/newAPIMapping/mapping-icon-reverse.svg";
import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import LogMethodTag from "@/components/LogMethodTag";
import { Text } from "@/components/Text";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import {
  IRequestMapping,
  IResponseMapping,
} from "@/utils/types/component.type";
import { CloseCircleFilled, RightOutlined } from "@ant-design/icons";
import { Flex, Modal, Typography } from "antd";
import clsx from "clsx";
import { get, isEmpty } from "lodash";
import { useBoolean } from "usehooks-ts";
import styles from "./index.module.scss";
import TrimmedPath from "@/components/TrimmedPath";

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
        <TrimmedPath path={sellerApi.url} />
        {isFocus ? (
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
        ) : (
          <RightOutlined style={{ color: "rgba(0, 0, 0, 0.45)", marginLeft: 'auto' }} />
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
  const {
    query,
    sellerApi,
    rightSide,
    setRightSide,
    setRequestMapping,
    setResponseMapping,
    setSellerApi,
    setServerKey,
    setListMappingStateResponse,
    setListMappingStateRequest,
  } = useNewApiMappingStore();
  const { activeTab } = useMappingUiStore();
  const queryData = JSON.parse(query ?? "{}");
  const { value: isFocus, setTrue, setFalse } = useBoolean(false);

  const resetMappingFnc = () => {
    setRequestMapping(
      mappers.request
        ?.map((rm: IRequestMapping) => ({
          ...rm,
          target: "",
          targetLocation: "",
          targetType: "",
          targetValues: [],
          valueMapping: {},
        }))
    );
    setResponseMapping(
      mappers.response
        ?.map((rm: IResponseMapping) => ({
          ...rm,
          source: "",
          sourceLocation: "",
          valueMapping: {},
        }))
    );
    setListMappingStateRequest([]);
    setListMappingStateResponse([]);
    setSellerApi(undefined);
    setServerKey("");
  };
  const handleClick = () => {
    Modal.confirm({
      className: styles.confirm,
      centered: true,
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
          <Text.NormalLarge lineHeight="24px" data-testid="sonataApi">Sonata API</Text.NormalLarge>
        </Flex>
        <Text.NormalLarge
          data-testid="sellerApi"
          style={{
            boxSizing: "border-box",
            flex: "0 0 calc(50% - 30px)",
            padding: 10,
            display: "flex",
            gap: 8,
          }}
        >
          Seller API
          <Typography.Text
            style={{
              color: "#00000073",
              fontSize: 12,
            }}
            ellipsis={{ tooltip: true }}
          >
            {get(sellerApi, "name", "")}
          </Typography.Text>
        </Text.NormalLarge>
      </Flex>
      <Flex align="center" gap={8} style={{ marginBottom: 26 }}>
        <Flex
          align="center"
          gap={6}
          className={styles.sonataAPIBasicInfoWrapper}
        >
          <LogMethodTag method={queryData?.method} />
          <TrimmedPath path={queryData?.path} />
        </Flex>
        <div className={styles.mappingIcon}>
          {activeTab === "request" ? <MappingIcon /> : <MappingReverseIcon />}
        </div>
        <Flex
          align="center"
          gap={8} style={{ width: "100%" }}
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
          <SellerAPI
            isFocus={isFocus}
            disabled={disabled}
            handleClick={handleClick}
          />
        </Flex>
      </Flex>
    </>
  );
};

export default HeaderMapping;
