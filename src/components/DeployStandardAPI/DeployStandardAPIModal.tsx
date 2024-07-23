import { Checkbox, Flex, Modal, Spin, Tag, notification } from "antd";
import Text from "../Text";
import styles from "./index.module.scss";
import { InfoCircleFilled } from "@ant-design/icons";
import {
  useDeployToEnv,
  useGetMapperDetails,
  useGetProductEnvs,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useNavigate, useParams } from "react-router-dom";
import { useCallback, useEffect, useMemo, useState } from "react";
import { get, isEmpty } from "lodash";
import RequestMethod from "../Method";

type Props = {
  open: boolean;
  onClose: () => void;
  defaultKey: string;
};

const DeployStandardAPIModal = ({ open, onClose, defaultKey }: Props) => {
  const [checkedList, setCheckedList] = useState<string[]>([]);
  const { currentProduct } = useAppStore();
  const { componentId } = useParams();
  const { data: dataMappers, isLoading } = useGetMapperDetails(
    currentProduct,
    componentId ?? ""
  );
  const { mutateAsync: deployment } = useDeployToEnv();
  const { data: dataEnv } = useGetProductEnvs(currentProduct);
  const navigate = useNavigate();
  const stageId = useMemo(() => {
    const stage = dataEnv?.data?.find(
      (env: any) => env.name?.toLowerCase() === "stage"
    );
    return stage?.id;
  }, [dataEnv]);

  const renderTextType = useCallback((type: string) => {
    switch (type) {
      case "access_e_line":
        return "Access E-line";
      case "uni":
        return "UNI";
      default:
        return type;
    }
  }, []);

  const handleOK = async () => {
    if (isEmpty(checkedList)) {
      notification.warning({
        message: "Please select at least one API to deploy",
      });
      return;
    }
    try {
      const res = await deployment({
        productId: currentProduct,
        componentId,
        mapperKeys: checkedList,
        envId: stageId,
      } as any);
      notification.success({ message: get(res, "message", "Success!") });
      onClose?.();
      navigate(`/env?envId=${stageId}`);
    } catch (error) {
      notification.error({
        message: get(error, "reason", "Error. Please try again"),
      });
    }
  };

  useEffect(() => {
    if (defaultKey && !isEmpty(dataMappers)) {
      const item = dataMappers.find(
        (item: any) => item.targetMapperKey === defaultKey
      );
      if (item.diffWithStage) {
        setCheckedList([defaultKey]);
      }
    }
  }, [defaultKey, dataMappers]);

  const options = useMemo(() => {
    if (!isEmpty(dataMappers)) {
      return dataMappers.map((item: any) => ({
        label: (
          <Flex align="center" gap={10}>
            <RequestMethod
              method={item.method}
              disabled={!item?.diffWithStage}
            />
            <Text.LightMedium>{item?.path}</Text.LightMedium>
            <Flex align="center" gap={8}>
              <div className={styles.tagInfo}>
                {renderTextType(item.productType)}
              </div>
              {item.actionType ? (
                <div
                  className={styles.tagInfo}
                  style={{ textTransform: "capitalize" }}
                >
                  {item.actionType}
                </div>
              ) : null}
            </Flex>
            {item?.mappingStatus === "incomplete" && (
              <Tag color="red" bordered={false}>
                Incomplete
              </Tag>
            )}
          </Flex>
        ),
        value: item.targetMapperKey,
        disabled: !item?.diffWithStage,
      }));
    }
    return [];
  }, [dataMappers]);

  return (
    <Modal
      width={900}
      open={open}
      onCancel={onClose}
      title={<Text.NormalLarge>Deploy to stage</Text.NormalLarge>}
      className={styles.modal}
      onOk={handleOK}
    >
      <Flex className={styles.title} gap={15} align="center">
        <InfoCircleFilled style={{ fontSize: 14, color: "#2962FF" }} />
        <Text.LightMedium>
          API mappings without any changes with what is running in stage cannot
          be selected.
        </Text.LightMedium>
      </Flex>
      <Flex gap={12} vertical style={{ marginTop: 12 }}>
        <Text.NormalMedium>Select API</Text.NormalMedium>
        {isLoading ? (
          <Spin spinning={isLoading} />
        ) : (
          <Checkbox.Group
            className={styles.checkbox}
            options={options}
            onChange={setCheckedList}
            value={checkedList}
          />
        )}
      </Flex>
    </Modal>
  );
};

export default DeployStandardAPIModal;
