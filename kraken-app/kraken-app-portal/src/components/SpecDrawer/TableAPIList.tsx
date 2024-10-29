import { Flex, Table } from "antd";
import { IItem } from ".";
import { useMemo, useState } from "react";
import RequestMethod from "../Method";
import styles from "./index.module.scss";
import { Text } from "../Text";
import { LeftOutlined, RightOutlined } from "@ant-design/icons";

type Props = {
  data: IItem[];
  onSelect: (value: string) => void;
  selected: string;
};

const TableAPIList = ({ data, onSelect, selected }: Props) => {
  const [page, setPage] = useState(1);
  const columns = useMemo(
    () => [
      {
        title: "Method",
        dataIndex: "method",
        render: (item: string) => <RequestMethod method={item} />,
      },
      {
        title: "Path",
        dataIndex: "path",
      },
    ],
    []
  );
  return (
    <Flex vertical style={{ height: "100%" }}>
      <Table
        style={{ flex: 1 }}
        className={styles.table}
        columns={columns}
        dataSource={data}
        rowSelection={{
          selectedRowKeys: [selected],
          type: "radio",
        }}
        rowKey={(item) => item.api}
        onRow={(record) => {
          return {
            onClick: () => {
              onSelect(record.api);
            },
          };
        }}
        pagination={{
          pageSize: 10,
          current: page,
        }}
      />
      <Flex justify="flex-end" gap={8} align="center">
        <LeftOutlined
          className={styles.navBtn}
          style={{ color: page === 1 ? "#C9CDD4" : "#4e5969" }}
          onClick={() => {
            if (page === 1) {
              return;
            }
            setPage(page - 1);
          }}
        />
        <div className={styles.activePage}>
          <Text.LightSmall>{page}</Text.LightSmall>
        </div>
        <Text.LightSmall>/</Text.LightSmall>
        <Text.LightSmall>{Math.ceil(data.length / 10)}</Text.LightSmall>
        <RightOutlined
          className={styles.navBtn}
          style={{
            color:
              page === Math.ceil(data?.length / 10) ? "#C9CDD4" : "#4e5969",
          }}
          onClick={() => {
            if (page === Math.ceil(data?.length / 10)) {
              return;
            }
            setPage(page + 1);
          }}
        />
      </Flex>
    </Flex>
  );
};

export default TableAPIList;
