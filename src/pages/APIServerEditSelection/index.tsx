import { useEditComponent, useGetComponentDetail } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useNavigate, useParams } from "react-router";
import SwaggerInfo from "../NewAPIServer/components/SwaggerInfo";
import styles from "./index.module.scss";
import Text from "@/components/Text";
import Flex from "@/components/Flex";
import { useEffect, useState } from "react";
import { Button, Transfer, TransferProps, notification } from "antd";
import { cloneDeep, get, isEmpty, set } from "lodash";
import { decode } from "js-base64";
import jsYaml from "js-yaml";
import { tranformSwaggerToArray } from "../NewAPIServer/components/SelectDownStreamAPI";

const APIServerEditSelection = () => {
  const { currentProduct } = useAppStore();
  const { componentId } = useParams();
  const { data: detailData } = useGetComponentDetail(
    currentProduct,
    componentId ?? ""
  );
  const [transferData, setTransferData] = useState<any>([]);
  const [selectedAPI, setSelectedAPI] = useState<any>();
  const [schemas, setSchemas] = useState<any>([]);
  const [targetKeys, setTargetKeys] = useState<TransferProps["targetKeys"]>([]);
  const navigate = useNavigate();
  const { mutateAsync: runUpdate } = useEditComponent();

  useEffect(() => {
    try {
      if (isEmpty(detailData)) {
        return;
      }
      const base64data = get(detailData, "facets.baseSpec.content");
      let swaggerData;
      let fileDecode = "";
      if (base64data) {
        fileDecode = decode(get(detailData, "facets.baseSpec.content"));
        swaggerData = jsYaml.load(fileDecode) as any;
        setSchemas(swaggerData?.components?.schemas);
        setTransferData(tranformSwaggerToArray(swaggerData));
      }
      setTargetKeys(get(detailData, "facets.selectedAPIs"));
    } catch (error) {
      notification.error({ message: "Error. Please try again" });
    }
  }, [detailData]);

  const handleChange: TransferProps["onChange"] = (newTargetKeys) => {
    setTargetKeys(newTargetKeys);
  };

  const handleAPI = async () => {
    try {
      if (detailData) {
        const data = cloneDeep(detailData);
        set(data, "facets.selectedAPIs", targetKeys);
        set(data, "metadata.version", get(data, "metadata.version", 1) + 1);
        await runUpdate({
          productId: currentProduct,
          componentId,
          data,
        } as any);
        notification.success({ message: "Edit success" });
        navigate(-1);
      }
    } catch (error) {
      notification.error({
        message: get(error, "message", "Error. Please try again"),
      });
    }
  };

  return (
    <Flex
      justifyContent="flex-start"
      gap={16}
      alignItems="flex-start"
      className={styles.root}
    >
      <div className={styles.transferSection}>
        <Text.BoldLarge>Select API for the API server</Text.BoldLarge>
        <p>
          <Text.NormalLarge>Console connect application</Text.NormalLarge>
        </p>
        <div className={styles.paper}>
          <Transfer
            filterOption={(inputValue: string, option: any) =>
              option.key.indexOf(inputValue) > -1
            }
            listStyle={{
              boxSizing: "border-box",
            }}
            dataSource={transferData}
            titles={["API list", "Selected API"]}
            showSelectAll
            showSearch
            selectionsIcon={<></>}
            locale={{
              itemUnit: "",
              itemsUnit: "",
              searchPlaceholder: "Please select",
            }}
            className={styles.transfer}
            onChange={handleChange}
            render={(item) => (
              <div
                style={{ width: "100%" }}
                key={`${item.title} - ${item.description}`}
                role="none"
                onClick={(e) => {
                  e?.stopPropagation();
                  e?.preventDefault();
                  setSelectedAPI(item);
                }}
              >{`${item.title} - ${item.description}`}</div>
            )}
            targetKeys={targetKeys}
          />
        </div>
        <Flex justifyContent="flex-end" gap={12} style={{ marginTop: 14 }}>
          <Button onClick={() => navigate(-1)}>Cancel</Button>
          <Button type="primary" onClick={handleAPI}>
            OK
          </Button>
        </Flex>
      </div>
      <div className={styles.apiDetail} style={{ flex: 2 }}>
        <SwaggerInfo item={selectedAPI} schemas={schemas} />
      </div>
    </Flex>
  );
};

export default APIServerEditSelection;
