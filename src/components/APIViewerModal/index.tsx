import { Modal, Table, notification } from "antd";
import { decode } from "js-base64";
import jsYaml from "js-yaml";
import { get } from "lodash";
import { useEffect, useMemo, useState } from "react";
import Text from "../Text";
import styles from "./index.module.scss";
import RequestMethod from "../Method";
import RequestBody from "@/pages/NewAPIServer/components/RequestBody";
import Response from "@/pages/NewAPIServer/components/Response";

type Props = {
  content: string;
  isOpen: boolean;
  onClose: () => void;
  selectedAPI?: string;
};

const APIViewerModal = ({ selectedAPI, content, isOpen, onClose }: Props) => {
  const [schemas, setSchemas] = useState<any>();
  const [viewData, setViewData] = useState<any>();

  useEffect(() => {
    try {
      if (selectedAPI && content) {
        const selectedArray = selectedAPI.split(" ");
        const decodeData = decode(content);
        const data = jsYaml.load(decodeData);
        setSchemas(get(data, "components.schemas"));
        setViewData(get(data, `paths.${selectedArray[0]}.${selectedArray[1]}`));
      }
    } catch (error) {
      notification.error({ message: "Can not load yaml" });
    }
  }, [selectedAPI, content]);

  const basicCol = useMemo(
    () => [
      {
        title: "Name",
        dataIndex: "name",
      },
      {
        title: "Method",
        dataIndex: "method",
        render: (method: string) => <RequestMethod method={method} />,
      },
      {
        title: "Description",
        dataIndex: "description",
      },
    ],
    []
  );

  const basicData = useMemo(() => {
    if (!viewData || !selectedAPI) {
      return undefined;
    }
    const selectedArray = selectedAPI.split(" ");
    return [
      {
        name: get(selectedArray, "[0]"),
        method: get(selectedArray, "[1]"),
        description: viewData?.description,
      },
    ];
  }, [viewData, selectedAPI]);

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

  return (
    <Modal
      className={styles.modal}
      open={isOpen}
      onOk={onClose}
      closable={false}
      cancelButtonProps={{ style: { display: "none" } }}
      width="80vw"
    >
      <div>
        <Text.BoldLarge>View API</Text.BoldLarge>
        <div className={styles.basicInfo}>
          <Table
            className={styles.table}
            pagination={false}
            columns={basicCol}
            dataSource={basicData}
          />
        </div>
        {paramData ? (
          <div className={styles.params}>
            <Text.LightMedium>Parameters</Text.LightMedium>
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
              <RequestBody item={viewData.requestBody} schemas={schemas} />
            </div>
          )}
          {viewData?.responses && (
            <div style={{ flex: 1 }}>
              <Text.LightLarge>Response</Text.LightLarge>
              <Response item={viewData.responses} schemas={schemas} />
            </div>
          )}
        </div>
      </div>
    </Modal>
  );
};

export default APIViewerModal;
