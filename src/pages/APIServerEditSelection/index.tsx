import { useEditComponent, useGetComponentDetail } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useNavigate, useParams } from "react-router";
import SwaggerInfo from "../NewAPIServer/components/SwaggerInfo";
import styles from "./index.module.scss";
import Text from "@/components/Text";
import Flex from "@/components/Flex";
import { useEffect, useState } from "react";
import { Button, Empty, Transfer, TransferProps, notification } from "antd";
import { cloneDeep, get, isEmpty, set } from "lodash";
import { decode } from "js-base64";
import jsYaml from "js-yaml";
import { LeftOutlined } from "@ant-design/icons";
import { tranformSwaggerToArray } from "../NewAPIServer/components/UploadYaml";

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
  const { mutateAsync: runUpdate, isPending } = useEditComponent();

  useEffect(() => {
    try {
      if (isEmpty(detailData)) {
        return;
      }
      const base64data = get(detailData, "facets.baseSpec.content");
      let swaggerData;
      let fileDecode = "";
      let newData;
      if (base64data) {
        fileDecode = decode(get(detailData, "facets.baseSpec.content"));
        swaggerData = jsYaml.load(fileDecode) as any;
        setSchemas(swaggerData?.components?.schemas);
        newData = tranformSwaggerToArray(swaggerData);
        setTransferData(newData);
        setSelectedAPI(get(newData, "[0]"));
        setTargetKeys(get(detailData, "facets.selectedAPIs", []));
      }
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
        message: get(
          error,
          "reason",
          get(error, "message", "Error. Please try again")
        ),
      });
    }
  };

  return (
    <div className={styles.root}>
      <Flex
        justifyContent="flex-start"
        gap={16}
        alignItems="flex-start"
        className={styles.container}
      >
        <div className={styles.transferSection}>
          <Flex
            gap={8}
            justifyContent="flex-start"
            style={{ cursor: "pointer", marginBottom: 4 }}
            role="none"
            onClick={() => navigate(-1)}
          >
            <LeftOutlined style={{ fontSize: 8 }} />
            <Text.LightLarge color="#434343">
              <span>Seller API Setup</span>
              <span style={{ color: "#848587" }}>/Edit select API</span>
            </Text.LightLarge>
          </Flex>
          <div className={styles.paper}>
            <div className={styles.appTitle}>
              <Text.NormalLarge>
                {get(detailData, "metadata.name")}
              </Text.NormalLarge>
            </div>
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
                searchPlaceholder: "Please select",
                itemUnit: "",
                itemsUnit: "",
                notFoundContent: (
                  <Empty description="Please select API from the API list" />
                ),
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
        </div>
        <div className={styles.apiDetail} style={{ flex: 2 }}>
          <SwaggerInfo
            item={selectedAPI}
            schemas={schemas}
            className={styles.info}
          />
        </div>
      </Flex>
      <Flex justifyContent="flex-end" gap={12} style={{ padding: 12 }}>
        <Button onClick={() => navigate(-1)}>Cancel</Button>
        <Button
          type="primary"
          onClick={handleAPI}
          disabled={isPending}
          loading={isPending}
        >
          OK
        </Button>
      </Flex>
    </div>
  );
};

export default APIServerEditSelection;
