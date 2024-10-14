import { Button, Drawer, Empty, Flex, notification } from "antd";
import Text from "../Text";
import { CloseOutlined } from "@ant-design/icons";
import styles from "./index.module.scss";
import jsYaml from "js-yaml";
import { decode } from "js-base64";
import { useEffect, useState } from "react";
import { get, isEmpty } from "lodash";
import APIViewerContent from "../APIViewerContent";
import TableAPIList from "./TableAPIList";
import { transformApiData } from "@/utils/helpers/swagger";

type Props = {
  content: string;
  isOpen: boolean;
  onClose: () => void;
};
export interface IItem {
  title: string;
  method: string;
  path: string;
  api: string;
}

const SpecDrawer = ({ content, isOpen, onClose }: Props) => {
  const [tableData, setTableData] = useState<IItem[]>([]);
  const [selectedAPI, setSelectedAPI] = useState("");
  const [title, setTitle] = useState("");
  const loadContent = () => {
    try {
      if (content) {
        const yamlContent = jsYaml.load(decode(content));
        const result = transformApiData(get(yamlContent, "paths", {}));
        setTitle(get(yamlContent, "info.title", ""));
        setTableData(result);
        setSelectedAPI(get(result, "[0].api", ""));
      }
    } catch (error) {
      notification.error({ message: "Can not load yaml" });
    }
  };

  useEffect(() => {
    loadContent();
  }, [content]);

  return (
    <Drawer
      className={styles.drawer}
      width="90vw"
      open={isOpen}
      onClose={onClose}
      footer={
        <Flex justify="flex-end">
          <Button type="primary" onClick={onClose}>
            OK
          </Button>
        </Flex>
      }
      title={
        <Flex justify="space-between">
          <Text.NormalLarge>View API spec</Text.NormalLarge>
          <CloseOutlined
            role="none"
            onClick={onClose}
            style={{ color: "#00000073", fontSize: 12 }}
          />
        </Flex>
      }
    >
      <Flex gap={14} style={{ height: "100%" }}>
        <div className={styles.tableList}>
          <div style={{ marginBottom: 12 }}>
            <Text.NormalLarge>{title}</Text.NormalLarge>
          </div>
          <TableAPIList
            data={tableData}
            onSelect={setSelectedAPI}
            selected={selectedAPI}
          />
        </div>
        <div className={styles.viewer}>
          <div className={styles.viewerHeader}>
            <Text.NormalLarge>API details</Text.NormalLarge>
          </div>
          <div className={styles.viewerContent}>
            {isEmpty(selectedAPI) ? (
              <Empty description="Please select api" />
            ) : (
              <APIViewerContent selectedAPI={selectedAPI} content={content} />
            )}
          </div>
        </div>
      </Flex>
    </Drawer>
  );
};

export default SpecDrawer;
