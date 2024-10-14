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
import styles from "./index.module.scss";
import { cloneDeep, get, isEmpty, isEqual, set } from "lodash";
import clsx from "clsx";

interface RequestMappingProps {
  rm: IRequestMapping;
  title: string;
}

const RequestMappingItem = ({ rm, title }: Readonly<RequestMappingProps>) => {
  const { requestMapping, setRightSide, setRightSideInfo, setRequestMapping } =
    useNewApiMappingStore();
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
    <Flex align="center" gap={4} className={styles.requestMappingItemWrapper}>
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
        {!isEmpty(rm.description) && (
          <Tooltip title={rm.description}>
            <InfoCircleOutlined style={{ color: "rgba(0, 0, 0, 0.45)" }} />
          </Tooltip>
        )}
      </Flex>
      {!rm.requiredMapping && (
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

  const handleDelete = () => {
    setRightSide(EnumRightType.SelectSellerAPI);
    setRightSideInfo(undefined);
  }

  const notEmptyList = !!list.length
  return (
    <Flex gap={16}>
      <div className={styles.sonataPropMappingWrapper}>
        {notEmptyList &&
          <>
            <p className={styles.label}>
              Property from Sonata API
            </p>
            <div className={styles.requestMappingList}>
              {list?.map((requestMapping) => (
                <RequestMappingItem
                  key={requestMapping.name}
                  rm={requestMapping}
                  title={title}
                />
              ))}
            </div>
          </>
        }
        {rightSide === EnumRightType.AddSonataProp &&
          rightSideInfo?.method === "add" && (
            <Flex align="center" gap={4} className={styles.requestMappingItemWrapper}>
              <Flex
                align="center"
                gap={4}
                className={styles.requestMappingItemInfo}
              >
                <Typography.Text ellipsis={{ tooltip: true }}>
                  Select or input property
                </Typography.Text>
              </Flex>
              <Button type="text" onClick={handleDelete}>
                <DeleteOutlined />
              </Button>
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
      {notEmptyList &&
        <>
          <div className={styles.alignArrowList}>
            {list?.map((rm) => (
              <div key={rm.name} className={styles.alignArrowWrapper}>
                <MappingIcon />
              </div>
            ))}
          </div>
          <div className={styles.sellerPropMappingWrapper}>
            <p className={styles.label}>

              Property from Seller API response
            </p>
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

        </>

      }

    </Flex>
  );
};

export default SonataPropMapping;
