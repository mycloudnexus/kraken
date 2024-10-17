import { Text } from "@/components/Text";
import { CloseOutlined, InfoCircleFilled } from "@ant-design/icons";
import { Drawer, Flex, Radio, Spin, Table, Typography } from "antd";
import { useMemo, useState } from "react";
import styles from "./index.module.scss";
import { useGetMappingTemplateUpgradeDetail } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import DeploymentStatus from "@/pages/EnvironmentOverview/components/DeploymentStatus";
import MappingMatrix from "@/components/MappingMatrix";
import { useGetAsset } from "@/hooks/asset";
import RequestMethod from "@/components/Method";
import RichTextViewer from "@/components/RichTextViewer";

type Props = {
  open?: boolean;
  onClose: () => void;
  id: string;
  noteId: string;
};

const UpgradeHistoryDetail = ({ onClose, open, id, noteId }: Props) => {
  const { currentProduct } = useAppStore();
  const [activeTab, setActiveTab] = useState("1");
  const { data, isLoading: loadingTable } = useGetMappingTemplateUpgradeDetail(
    currentProduct,
    id
  );
  const { data: dataAsset, isLoading: loadingNote } = useGetAsset(noteId);
  const columns = useMemo(
    () => [
      {
        title: "Mapping use case",
        dataIndex: "",
        render: (record: any) => (
          <Flex align="center" gap={12}>
            <Flex align="center">
              <RequestMethod method={record?.method} />
              <Typography.Text
                style={{ maxWidth: 140 }}
                ellipsis={{ tooltip: record?.path }}
              >
                {record?.path}
              </Typography.Text>
            </Flex>
            <MappingMatrix mappingMatrix={record?.mappingMatrix} />
          </Flex>
        ),
      },
      {
        title: "Upgrade to",
        dataIndex: "version",
        width: 150,
      },
      {
        title: "Upgrade status",
        width: 150,
        dataIndex: "status",
        render: (status: string) => <DeploymentStatus status={status} />,
      },
    ],
    []
  );
  return (
    <Drawer
      width="40vw"
      open={open}
      onClose={onClose}
      title={
        <Flex justify="space-between">
          <span>View details</span>
          <CloseOutlined onClick={onClose} role="none" />
        </Flex>
      }
      closable
    >
      <Radio.Group
        onChange={(e) => {
          setActiveTab(e.target.value);
        }}
        value={activeTab}
        style={{
          marginBottom: 8,
        }}
      >
        <Radio.Button value="1">Release note</Radio.Button>
        <Radio.Button value="2">Impacted mapping use cases</Radio.Button>
      </Radio.Group>
      {activeTab === "1" ? (
        <Spin spinning={loadingNote}>
          <RichTextViewer
            className={styles.releaseNote}
            text={dataAsset?.metadata?.description}
          />
        </Spin>
      ) : (
        <>
          <Flex align="center" className={styles.note}>
            <InfoCircleFilled style={{ color: "#2962FF" }} />
            <Text.LightMedium>
              Following mapping use cases upgraded because of this template
              upgrade.
            </Text.LightMedium>
          </Flex>
          <Table
            scroll={{ x: "auto" }}
            columns={columns}
            dataSource={data ?? []}
            pagination={false}
            loading={loadingTable}
          />
        </>
      )}
    </Drawer>
  );
};

export default UpgradeHistoryDetail;
