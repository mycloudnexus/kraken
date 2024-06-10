import RequestMethod from "@/components/Method";
import { Button, Table } from "antd";
import { ColumnType } from "antd/es/table";
import { useMemo } from "react";

type Props = {
  item: any;
};

const ExpandRow = ({ item }: Props) => {
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
        width: 200,
      },
      {
        dataIndex: "name",
      },
      {
        dataIndex: "",
        align: "right",
        render: () => (
          <Button type="text" style={{ color: "#1677ff" }}>
            View
          </Button>
        ),
      },
    ],
    []
  );

  return (
    <Table
      prefixCls="expand-table"
      showHeader={false}
      columns={columns}
      dataSource={data}
      pagination={false}
    />
  );
};

export default ExpandRow;
