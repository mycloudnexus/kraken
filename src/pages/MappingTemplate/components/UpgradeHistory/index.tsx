import { useGetMappingTemplateUpgradeList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { defaultData, useMappingTemplateStore } from "@/stores/mappingTemplate";
import { Table } from "antd";
import { get, upperFirst } from "lodash";
import { useEffect, useMemo, useRef } from "react";
import styles from "./index.module.scss";
import useSize from "@/hooks/useSize";

const UpgradeHistory = () => {
  const { currentProduct } = useAppStore();
  const { upgradeParams, setUpgradeParams } = useMappingTemplateStore();
  const { data } = useGetMappingTemplateUpgradeList(
    currentProduct,
    upgradeParams
  );
  const ref = useRef<any>();
  const size = useSize(ref);

  const columns = useMemo(
    () => [
      { title: "Template version", width: 200, dataIndex: "productVersion" },
      { title: "Upgrade status", width: 200, dataIndex: "status" },
      { title: "Environment", width: 200, dataIndex: "envName", render: (text: string) => upperFirst(text) },
      { title: "Upgraded by", width: 300, dataIndex: "" },
      { title: "Actions", width: 200, dataIndex: "" },
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
      <Table
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
        scroll={{ y: get(size, "height", 0) - 120 }}
      />
    </div>
  );
};

export default UpgradeHistory;
