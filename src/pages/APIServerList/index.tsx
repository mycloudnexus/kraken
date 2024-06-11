import { useGetComponentList } from "@/hooks/product";
import styles from "./index.module.scss";
import { useAppStore } from "@/stores/app.store";
import { API_SERVER_KEY } from "@/utils/constants/product";
import Flex from "@/components/Flex";
import Text from "@/components/Text";
import { Button, Table } from "antd";
import { useNavigate } from "react-router";
import { useEffect, useMemo, useState } from "react";
import { isEmpty } from "lodash";
import ExpandRow from "./components/ExpandRow";
import APIServerModal from "@/components/APIServerModal";
import { useBoolean } from "usehooks-ts";

const APIServerList = () => {
  const {
    value: isOpenModal,
    setTrue: openModal,
    setFalse: closeModal,
  } = useBoolean(false);
  const [expandedRowKeys, setExpandedRowKeys] = useState<string[]>([]);
  const { currentProduct } = useAppStore();
  const {
    data: dataList,
    isLoading,
    refetch: refresh,
  } = useGetComponentList(currentProduct, {
    kind: API_SERVER_KEY,
    size: 1000,
  });
  const navigate = useNavigate();
  const [selectedAPI, setSelectedAPI] = useState("");

  const columns = useMemo(() => {
    return [
      {
        dataIndex: "metadata",
        render: (metadata: any) => metadata?.name,
      },
      {
        render: () => (
          <Button type="text" style={{ color: "#1677ff" }}>
            Add API
          </Button>
        ),
        width: 100,
      },
      {
        dataIndex: "metadata",
        render: ({ key = "" }) => (
          <Button
            type="text"
            style={{ color: "#1677ff" }}
            onClick={() => {
              setSelectedAPI(key);
              openModal();
            }}
          >
            View
          </Button>
        ),
        width: 100,
      },
    ];
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    setExpandedRowKeys(dataList?.data?.map((item: { id: any }) => item?.id));
  }, [dataList?.data]);

  return (
    <div className={styles.root}>
      {isOpenModal && (
        <APIServerModal
          id={selectedAPI}
          isOpen={isOpenModal}
          onClose={closeModal}
          refresh={refresh}
        />
      )}
      <Flex justifyContent="space-between">
        <Text.BoldLarge size="20px">Seller API Server Setup</Text.BoldLarge>
        <Button
          type="primary"
          onClick={() => navigate(`/component/${currentProduct}/new`)}
        >
          + Add API Server
        </Button>
      </Flex>
      <div className={styles.content}>
        <Table
          loading={isLoading}
          showHeader={false}
          columns={columns}
          dataSource={dataList?.data}
          pagination={false}
          expandable={{
            expandedRowKeys,
            onExpandedRowsChange: (newKeys: any) => setExpandedRowKeys(newKeys),
            rowExpandable: (record) => !isEmpty(record),
            expandedRowRender: (record) => <ExpandRow item={record} />,
          }}
          rowKey={(item) => item?.id}
        />
      </div>
    </div>
  );
};

export default APIServerList;
