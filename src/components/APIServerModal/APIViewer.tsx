import { Button, Table } from "antd";
import Text from "../Text";
import styles from "./index.module.scss";
import { useMemo } from "react";
import { get } from "lodash";
import { decode } from "js-base64";
import jsYaml from "js-yaml";
import Flex from "../Flex";
import { PaperClipOutlined } from "@ant-design/icons";

type Props = {
  detail: any;
  onClose?: () => void;
  enableEdit?: () => void;
};

const APIViewer = ({ detail, onClose, enableEdit }: Props) => {
  const fileName = useMemo(() => {
    if (!get(detail, "facets.baseSpec.content")) {
      return "";
    }
    const fileData = decode(get(detail, "facets.baseSpec.content"));
    const swaggerData = jsYaml.load(fileData);
    return get(swaggerData, "info.title");
  }, [detail]);

  const basicDetailCol = useMemo(
    () => [
      {
        title: "Application Name",
        dataIndex: "name",
      },
      {
        title: "Description",
        dataIndex: "description",
      },
      {
        title: "Online API document link",
        dataIndex: "link",
      },
    ],
    []
  );

  const environmentCol = useMemo(
    () => [
      {
        title: "Environment Name",
        dataIndex: "name",
      },
      {
        title: "URL",
        dataIndex: "url",
      },
    ],
    []
  );

  const environmentData = useMemo(() => {
    const env = get(detail, "facets.environments");
    if (!env) {
      return [];
    }
    const keys = Object.keys(env);
    return keys?.map((k: string) => ({ name: k, url: env[k] }));
  }, [detail]);

  return (
    <div>
      <Text.BoldLarge>View API server</Text.BoldLarge>
      <div className={styles.basicDetailTable}>
        <Table
          columns={basicDetailCol}
          dataSource={[
            {
              name: get(detail, "metadata.name"),
              description: get(detail, "metadata.description"),
              link: get(detail, "facets.baseSpec.path"),
            },
          ]}
          pagination={false}
        />
      </div>
      <div>
        <Text.LightMedium>API spec</Text.LightMedium>
        <Flex gap={9} justifyContent="flex-start">
          <PaperClipOutlined />
          <Text.LightMedium>{fileName}</Text.LightMedium>
        </Flex>
      </div>
      <div className={styles.environment}>
        <Text.LightMedium>Environment Variables</Text.LightMedium>
        <Table
          columns={environmentCol}
          className={styles.environmentTable}
          dataSource={environmentData}
          pagination={false}
        />
      </div>
      <Flex className={styles.modalFooter} justifyContent="flex-end" gap={12}>
        <Button>Add new API</Button>
        <Button onClick={enableEdit}>Edit</Button>
        <Button type="primary" onClick={onClose}>
          OK
        </Button>
      </Flex>
    </div>
  );
};

export default APIViewer;
