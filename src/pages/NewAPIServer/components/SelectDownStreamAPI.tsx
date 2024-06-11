import Flex from "@/components/Flex";
import Text from "@/components/Text";
import styles from "./index.module.scss";
import {
  Form,
  FormInstance,
  Transfer,
  TransferProps,
  notification,
} from "antd";
import { useEffect, useState } from "react";
import { isEmpty } from "lodash";
import SwaggerInfo from "./SwaggerInfo";
import BtnStep from "./BtnStep";
import yaml from "js-yaml";
import { decode } from "js-base64";

type Props = {
  form: FormInstance<any>;
  active: boolean;
  onNext: () => void;
  onPrev: () => void;
  currentStep: number;
};

// eslint-disable-next-line react-refresh/only-export-components
export const tranformSwaggerToArray = (data: any) => {
  const paths = data.paths;
  const pathsArray = [];

  for (const path in paths) {
    for (const method in paths[path]) {
      const pathObject = {
        key: `${path} ${method}`,
        title: path,
        description: method,
        info: paths[path][method],
      };
      pathsArray.push(pathObject);
    }
  }

  return pathsArray;
};

const SelectDownStreamAPI = ({
  form,
  active,
  currentStep,
  onPrev,
  onNext,
}: Props) => {
  const [selectedAPI, setSelectedAPI] = useState<any>();
  const [transferData, setTransferData] = useState<any>([]);
  const [targetKeys, setTargetKeys] = useState<TransferProps["targetKeys"]>([]);
  const [schemas, setSchemas] = useState<any>([]);

  const file = Form.useWatch("file", form);
  const loadFile = async () => {
    try {
      const swaggerData = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(file.file);
      });

      const data = yaml.load(decode(swaggerData as any)) as any;
      setSchemas(data?.components?.schemas);
      setTransferData(tranformSwaggerToArray(data));
    } catch (error) {
      form.setFieldValue("file", undefined);
      notification.error({ message: "Please select a valid swagger file" });
    }
  };

  const handleChange: TransferProps["onChange"] = (newTargetKeys) => {
    setTargetKeys(newTargetKeys);
  };

  useEffect(() => {
    if (!isEmpty(file?.file)) {
      loadFile();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [file]);

  return (
    <div style={{ display: active ? "block" : "none" }}>
      <Flex
        justifyContent="flex-start"
        gap={16}
        alignItems="flex-start"
        className={styles.selectAPIWrapper}
      >
        <div style={{ flex: 3 }}>
          <Text.BoldLarge>Select API for the API server</Text.BoldLarge>
          <p>
            <Text.NormalLarge>Console connect application</Text.NormalLarge>
          </p>
          <div className={styles.paper}>
            <Form.Item noStyle name="selectedAPIs" valuePropName="targetKeys">
              <Transfer
                listStyle={{
                  width: "calc(50% - 22px)",
                }}
                filterOption={(inputValue: string, option: any) =>
                  option.key.indexOf(inputValue) > -1
                }
                dataSource={transferData}
                titles={["API list", "Selected API"]}
                showSearch
                showSelectAll
                selectionsIcon={<></>}
                className={styles.transfer}
                targetKeys={targetKeys}
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
                locale={{
                  itemUnit: "",
                  itemsUnit: "",
                  searchPlaceholder: "Please select",
                }}
              />
            </Form.Item>
          </div>
          <BtnStep onNext={onNext} onPrev={onPrev} currentStep={currentStep} />
        </div>
        <div className={styles.apiDetail} style={{ flex: 2 }}>
          <SwaggerInfo item={selectedAPI} schemas={schemas} />
        </div>
      </Flex>
    </div>
  );
};

export default SelectDownStreamAPI;
