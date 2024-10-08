import { useGetMappingTemplateUpgradeList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { defaultData, useMappingTemplateStore } from "@/stores/mappingTemplate";
import { Button, Table } from "antd";
import { get, upperFirst } from "lodash";
import { useEffect, useMemo, useRef, useState } from "react";
import styles from "./index.module.scss";
import useSize from "@/hooks/useSize";
import DeploymentStatus from "@/pages/EnvironmentOverview/components/DeploymentStatus";
import { ContentTime } from "@/pages/NewAPIMapping/components/DeployHistory";
import { IUpgrade } from "@/utils/types/product.type";
import UpgradeHistoryDetail from "../UpgradeHistoryDetail";
import { useBoolean } from "usehooks-ts";

const UpgradeHistory = () => {
  const { currentProduct } = useAppStore();
  const { upgradeParams, setUpgradeParams } = useMappingTemplateStore();
  const { data, isLoading } = useGetMappingTemplateUpgradeList(
    currentProduct,
    upgradeParams
  );
  const ref = useRef<any>();
  const size = useSize(ref);
  const [selectedRecordId, setSelectedRecordId] = useState("");
  const [selectedNoteId, setSelectedNoteId] = useState("");
  const { value: drawerOpen, setValue: setDrawerOpen } = useBoolean(false);

  const columns = useMemo(
    () => [
      { title: "Template version", width: 200, dataIndex: "productVersion" },
      {
        title: "Upgrade status",
        width: 200,
        dataIndex: "status",
        render: (text: string) => <DeploymentStatus status={text} />,
      },
      {
        title: "Environment",
        width: 200,
        dataIndex: "envName",
        render: (text: string) => upperFirst(text),
      },
      {
        title: "Upgraded by",
        width: 300,
        dataIndex: "",
        render: (record: IUpgrade) => (
          <ContentTime content={record.upgradeBy} time={record.createdAt} />
        ),
      },
      {
        title: "Actions",
        width: 200,
        dataIndex: "",
        render: (record: IUpgrade) => (
          <Button
            type="link"
            style={{ padding: 0 }}
            onClick={() => {
              setDrawerOpen(true);
              setSelectedRecordId(record.deploymentId);
              setSelectedNoteId(record.templateUpgradeId);
            }}
          >
            View details
          </Button>
        ),
      },
    ],
    []
  );

  useEffect(() => {
    return () => {
      setUpgradeParams(defaultData.upgradeParams);
    };
  }, []);

  return (
    <div className={styles.root} ref={ref}>
      {drawerOpen && (
        <UpgradeHistoryDetail
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          id={selectedRecordId}
          noteId={selectedNoteId}
        />
      )}
      <Table
        loading={isLoading}
        size="middle"
        columns={columns}
        dataSource={get(data, "data", [])}
        pagination={{
          pageSize: get(data, "size", 20),
          current: get(data, "page", 0) + 1,
          total: get(data, "total", 0),
          showSizeChanger: true,
          showTotal: (total) => `Total ${total} items`,
          onChange: (page, pageSize) =>
            setUpgradeParams({ page: page - 1, size: pageSize }),
        }}
        scroll={{ y: get(size, "height", 0) - 110 }}
      />
    </div>
  );
};

export default UpgradeHistory;
