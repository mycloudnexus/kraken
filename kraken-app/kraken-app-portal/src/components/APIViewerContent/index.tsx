import { get } from "lodash";
import { useEffect, useMemo, useState } from "react";
import swaggerClient from "swagger-client";
import jsYaml from "js-yaml";
import { Text } from "../Text";
import RequestBody from "@/pages/NewAPIServer/components/RequestBody";
import Response from "@/pages/NewAPIServer/components/Response";
import styles from "./index.module.scss";
import { Col, Row, Table, notification } from "antd";
import Flex from "../Flex";
import RequestMethod from "../Method";
import TrimmedPath from "../TrimmedPath";
import { decodeFileContent } from "@/utils/helpers/base64";

type Props = {
  selectedAPI?: string;
  content: string;
};

const APIViewerContent = ({ selectedAPI, content }: Props) => {
  const [schemas, setSchemas] = useState<any>();
  const [viewData, setViewData] = useState<any>();

  const loadContent = async () => {
    try {
      if (selectedAPI && content) {
        const selectedArray = selectedAPI.split(" ");
        const yamlContent = jsYaml.load(decodeFileContent(content));
        const result = await swaggerClient.resolve({ spec: yamlContent });
        const data = get(result, "spec");
        setSchemas(get(data, "components.schemas"));
        setViewData(get(data, `paths.${selectedArray[0]}.${selectedArray[1]}`));
      }
    } catch (error) {
      notification.error({ message: "Can not load yaml" });
    }
  };

  useEffect(() => {
    loadContent();
  }, [selectedAPI, content]);

  const paramCol = useMemo(
    () => [
      {
        title: "Name",
        dataIndex: "name",
      },
      {
        title: "Type",
        dataIndex: "type",
      },
      {
        title: "Description",
        dataIndex: "description",
      },
    ],
    []
  );

  const paramData = useMemo(() => {
    if (!viewData) {
      return undefined;
    }
    return viewData?.parameters?.map((param: any) => ({
      ...param,
      type: get(param, "schema.type"),
    }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [viewData]);

  const basicData = useMemo(() => {
    if (!viewData || !selectedAPI) {
      return undefined;
    }
    const selectedArray = selectedAPI.split(" ");
    return {
      name: get(selectedArray, "[0]"),
      method: get(selectedArray, "[1]"),
      description: viewData?.description,
    };
  }, [viewData, selectedAPI]);
  return (
    <div className={styles.root}>
      <div className={styles.basicInfo}>
        <Row gutter={[12, 20]}>
          <Col span={4}>
            <Flex flexDirection="column" alignItems="flex-start" gap={8}>
              <Text.LightMedium color="#00000073">Method</Text.LightMedium>
              <RequestMethod method={basicData?.method} />
            </Flex>
          </Col>
          <Col span={10}>
            <Flex flexDirection="column" alignItems="flex-start" gap={8}>
              <Text.LightMedium color="#00000073">Path</Text.LightMedium>
              <TrimmedPath path={basicData?.name ?? ''} />
            </Flex>
          </Col>
          <Col span={10}>
            <Flex flexDirection="column" alignItems="flex-start" gap={8}>
              <Text.LightMedium color="#00000073">Description</Text.LightMedium>
              <Text.LightMedium>{basicData?.description}</Text.LightMedium>
            </Flex>
          </Col>
        </Row>
      </div>
      {paramData ? (
        <div className={styles.params}>
          <div className={styles.title}>
            <Text.NormalMedium>Parameters</Text.NormalMedium>
          </div>
          <Table
            className={styles.table}
            columns={paramCol}
            dataSource={paramData}
            pagination={false}
          />
        </div>
      ) : null}
      <div className={styles.apiDetail}>
        {viewData?.requestBody && (
          <div style={{ flex: 1 }}>
            <div className={styles.title}>
              <Text.NormalMedium>Request body</Text.NormalMedium>
            </div>
            <RequestBody
              item={viewData.requestBody}
              schemas={schemas}
              showTitle={false}
            />
          </div>
        )}
        {viewData?.responses && (
          <div style={{ flex: 1 }}>
            <div className={styles.title}>
              <Text.NormalMedium>Response</Text.NormalMedium>
            </div>
            <Response item={viewData.responses} schemas={schemas} />
          </div>
        )}
      </div>
    </div>
  );
};

export default APIViewerContent;
