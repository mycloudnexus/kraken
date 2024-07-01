import MappingIcon from "@/assets/newAPIMapping/mapping-icon.svg";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EnumRightType } from "@/utils/types/common.type";
import { IRequestMapping } from "@/utils/types/component.type";
import {
  DeleteOutlined,
  InfoCircleOutlined,
  RightOutlined,
} from "@ant-design/icons";
import { Button, Flex, Input, Tooltip, Typography } from "antd";
import { useState } from "react";
import styles from "./index.module.scss";
import { cloneDeep, get, isEqual, set } from "lodash";
import clsx from "clsx";

interface RequestMappingProps {
  rm: IRequestMapping;
  title: string;
}

const RequestMappingItem = ({ rm, title }: Readonly<RequestMappingProps>) => {
  const { requestMapping, setRightSide, setRightSideInfo, setRequestMapping } =
    useNewApiMappingStore();
  const [showRemoveBtn, setShowRemoveBtn] = useState(false);
  const handleDelete = () => {
    setRequestMapping(
      requestMapping.filter((item) =>
        ["source", "sourceLocation", "target", "targetLocation"].some(
          (path) => get(item, path) !== get(rm, path)
        )
      )
    );
  };
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
        <Typography.Text ellipsis={{ tooltip: true }}>
          {rm.source}
        </Typography.Text>
        <Tooltip title={rm.description}>
          <InfoCircleOutlined style={{ color: "rgba(0, 0, 0, 0.45)" }} />
        </Tooltip>
      </Flex>
      {showRemoveBtn && (
        <Button type="text" onClick={handleDelete}>
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
  const {
    rightSide,
    rightSideInfo,
    setRightSide,
    setRightSideInfo,
    requestMapping,
    setRequestMapping,
  } = useNewApiMappingStore();

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
          {list?.map((rm, index) => (
            <Input
              key={rm.name}
              className={clsx(styles.sellerPropItemWrapper, {
                [styles.active]: isEqual(rm, rightSideInfo?.previousData),
              })}
              onClick={() => {
                setRightSide(EnumRightType.AddSellerProp);
                setRightSideInfo({
                  method: "update",
                  previousData: rm,
                  title,
                });
              }}
              value={rm.target}
              placeholder="Select property"
              suffix={
                <RightOutlined style={{ fontSize: 12, color: "#C9CDD4" }} />
              }
              onChange={(e) => {
                const newValue = get(e, "target.value", "")
                  .replace("@{{", "")
                  .replace("}}", "");
                let targetLocation = get(rm, "targetLocation", "");
                if (newValue.includes(".")) {
                  const splited = newValue.split(".");
                  const pathValue = get(splited, "[0]", "").toLocaleUpperCase();
                  targetLocation =
                    pathValue === "REQUESTBODY" ? "BODY" : pathValue;
                }
                const newRequest = cloneDeep(requestMapping);
                set(
                  newRequest,
                  `[${index}].target`,
                  get(e, "target.value", "")
                );
                set(newRequest, `[${index}].targetLocation`, targetLocation);
                setRequestMapping(newRequest);
              }}
            />
          ))}
        </div>
      </div>
    </Flex>
  );
};

export default SonataPropMapping;
