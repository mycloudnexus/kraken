import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { IRequestMapping } from "@/utils/types/component.type";
import {
  DeleteOutlined,
  InfoCircleOutlined,
  RightOutlined,
} from "@ant-design/icons";
import { Button, Flex, Tooltip, Typography } from "antd";
import { useState } from "react";
import styles from "./index.module.scss";

interface RequestMappingProps {
  rm: IRequestMapping;
  title: string;
}

const RequestMappingItem = ({ rm, title }: Readonly<RequestMappingProps>) => {
  const { setRightSide, setRightSideInfo } = useNewApiMappingStore();
  const [showRemoveBtn, setShowRemoveBtn] = useState(false);
  return (
    <Flex
      align="center"
      gap={4}
      onMouseEnter={() => setShowRemoveBtn(true)}
      onMouseLeave={() => setShowRemoveBtn(false)}
      className={styles.requestMappingItemWrapper}
    >
      <Flex
        align="center"
        gap={4}
        className={styles.requestMappingItemInfo}
        onClick={() => {
          setRightSide(EnumRightType.AddSonataProp);
          setRightSideInfo({
            method: "update",
            previousData: rm,
            title,
          });
        }}
      >
        {rm.sourceLocation}.{rm.source}{" "}
        <Tooltip title={rm.description}>
          <InfoCircleOutlined style={{ color: "rgba(0, 0, 0, 0.45)" }} />
        </Tooltip>
      </Flex>
      {showRemoveBtn && (
        <Button type="text">
          <DeleteOutlined />
        </Button>
      )}
    </Flex>
  );
};
interface Props {
  list: IRequestMapping[];
  title: string;
}
const SonataPropMapping = ({ list, title }: Readonly<Props>) => {
  const { rightSide, rightSideInfo, setRightSide, setRightSideInfo } =
    useNewApiMappingStore();

  return (
    <Flex gap={16}>
      <div className={styles.sonataPropMappingWrapper}>
        <Typography.Text>Property from Sonata API</Typography.Text>
        <div className={styles.requestMappingList}>
          {list?.map((requestMapping) => (
            <RequestMappingItem
              key={requestMapping.name}
              rm={requestMapping}
              title={title}
            />
          ))}
        </div>
        {rightSide === EnumRightType.AddSonataProp &&
          rightSideInfo?.method === "add" && (
            <Flex
              align="center"
              justify="space-between"
              className={styles.addMappingDiv}
            >
              <Typography.Text style={{ color: "#86909c" }}>
                Select property
              </Typography.Text>
              <RightOutlined style={{ color: "#4E5969" }} />
            </Flex>
          )}
        <Button
          type="primary"
          onClick={() => {
            setRightSide(EnumRightType.AddSonataProp);
            setRightSideInfo({
              method: "add",
              title,
            });
          }}
          style={{ alignSelf: "flex-start" }}
        >
          Add mapping property
        </Button>
      </div>
      <div className={styles.alignArrowList}>
        {list?.map((rm) => (
          <div key={rm.name} className={styles.alignArrowWrapper}>
            <MappingIcon />
          </div>
        ))}
      </div>
      <div className={styles.sellerPropMappingWrapper}>
        <Typography.Text>Property from Seller API response</Typography.Text>
        <div className={styles.responseMappingList}>
          {list?.map((rm) => (
            <Flex
              key={rm.name}
              className={styles.sellerPropItemWrapper}
              onClick={() => {
                setRightSide(EnumRightType.AddSellerProp);
                setRightSideInfo({
                  method: "update",
                  previousData: rm,
                  title,
                });
              }}
            >
              {rm.target && rm.targetLocation ? (
                <>
                  {rm.targetLocation}.{rm.target}
                </>
              ) : (
                <Typography.Text style={{ color: "#86909c" }}>
                  Select property
                </Typography.Text>
              )}
              <RightOutlined style={{ color: "#4E5969" }} />
            </Flex>
          ))}
        </div>
      </div>
    </Flex>
  );
};

export default SonataPropMapping;