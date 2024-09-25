import Text from "@/components/Text";
import styles from "./index.module.scss";
import { Input, Switch, Table } from "antd";
import { useMemo, useRef } from "react";
import { useGetUserList } from "@/hooks/user";
import { useUserStore } from "@/stores/user.store";
import { get } from "lodash";
import Role from "@/components/Role";
import dayjs from "dayjs";
import useSize from "@/hooks/useSize";

const UserManagement = () => {
  const { userParams, setUserParams } = useUserStore();
  const { data: dataUser } = useGetUserList(userParams);
  const ref = useRef<any>();
  const size = useSize(ref);

  const columns = useMemo(
    () => [
      {
        title: "User name",
        dataIndex: "name",
      },
      {
        title: "User email",
        dataIndex: "email",
      },
      {
        title: "User role",
        dataIndex: "role",
        filters: [
          {
            text: "Admin",
            value: "ADMIN",
          },
          {
            text: "User",
            value: "USER",
          },
        ],
        render: (role: string) => <Role role={role} />,
      },
      {
        title: "Enable State",
        dataIndex: "state",
        filters: [
          {
            text: "Enable",
            value: true,
          },
          {
            text: "Disable",
            value: false,
          },
        ],
        render: (state: string) => (
          <Switch checked={state === "ENABLED"} style={{ opacity: 0.4 }} />
        ),
      },
      {
        title: "Created at",
        dataIndex: "createdAt",
        render: (createdAt: string) =>
          dayjs(createdAt).format("YYYY-MM-DD HH:mm:ss"),
      },
    ],
    []
  );
  return (
    <div className={styles.root}>
      <Text.LightLarge>User management</Text.LightLarge>
      <div className={styles.container} ref={ref}>
        <Input.Search placeholder="Search user" style={{ width: 264 }} />
        <Table
          className={styles.table}
          columns={columns}
          dataSource={get(dataUser, "data", [])}
          rowKey="id"
          pagination={{
            size: "small",
            pageSize: get(dataUser, "size", 20),
            current: get(dataUser, "page", 0) + 1,
            total: get(dataUser, "total", 0),
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} items`,
            onChange: (page, pageSize) =>
              setUserParams({ page: page - 1, size: pageSize }),
          }}
          scroll={{
            y: get(size, "height", 0) - 164,
          }}
        />
      </div>
    </div>
  );
};

export default UserManagement;
