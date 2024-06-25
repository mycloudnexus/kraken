import APIViewerModal from "@/components/APIViewerModal";
import RequestMethod from "@/components/Method";
import { Button, Table } from "antd";
import { ColumnType } from "antd/es/table";
import { get } from "lodash";
import { useMemo, useState } from "react";
import { useBoolean } from "usehooks-ts";

type Props = {
  item: any;
};

const ExpandRow = ({ item }: Props) => {
  const {
    value: isOpenModal,
    setFalse: closeModal,
    setTrue: openModal,
  } = useBoolean(false);
  const [selectedAPI, setSelectedAPI] = useState("");
  const data = useMemo(() => {
    if (!item.facets?.selectedAPIs) {
      return null;
    }
    return item.facets?.selectedAPIs?.map((key: string) => {
      const dataKey = key.split(" ");
      return {
        name: dataKey[0],
        method: dataKey[1],
      };
    });
  }, [item]);

  const columns: ColumnType<any>[] = useMemo(
    () => [
      {
        dataIndex: "name",
        width: 400,
      },
      {
        dataIndex: "method",
        render: (method: string) => <RequestMethod method={method} />,
        width: 120,
      },
      {
        dataIndex: "name",
      },
      {
        dataIndex: "",
        align: "right",
        render: (record: any) => (
          <Button
            type="text"
            style={{ color: "#1677ff" }}
            onClick={() => {
              setSelectedAPI(`${record.name} ${record.method}`);
              openModal();
            }}
          >
            View
          </Button>
        ),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  );

  return (
    <>
      <APIViewerModal
        content={get(item, "facets.baseSpec.content")}
        isOpen={isOpenModal}
        onClose={closeModal}
        selectedAPI={selectedAPI}
      />
      <Table
        size="small"
        prefixCls="expand-table"
        showHeader={false}
        columns={columns}
        dataSource={data}
        pagination={false}
        rowKey={(r) => r.name + r.method}
      />
    </>
  );
};

export default ExpandRow;
