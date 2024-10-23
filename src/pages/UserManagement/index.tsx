import { PageLayout } from "@/components/Layout";
import { ERole } from "@/components/Role";
import { useDisableUser, useEnableUser, useGetUserList } from "@/hooks/user";
import useUser from "@/hooks/user/useUser";
import { useUserStore } from "@/stores/user.store";
import { IUser } from "@/utils/types/user.type";
import { Button, Flex, Input, Switch, Table, notification } from "antd";
import dayjs from "dayjs";
import { debounce, get } from "lodash";
import { useEffect, useMemo } from "react";
import { useBoolean } from "usehooks-ts";
import ResetPwd from "./components/ResetPwd";
import UserModal from "./components/UserModal";
import UserRoleEdit from "./components/UserRoleEdit";
import styles from "./index.module.scss";

const UserManagement = () => {
  const { currentUser } = useUser();
  const { userParams, setUserParams, resetParams } = useUserStore();
  const { data: dataUser, isLoading: loadingUser } = useGetUserList(userParams);
  const { value: isOpen, setTrue: open, setFalse: close } = useBoolean(false);
  const { mutateAsync: runEnable, isPending: pendingEnable } = useEnableUser();
  const { mutateAsync: runDisable, isPending: pendingDisable } =
    useDisableUser();
  const isAdmin = useMemo(
    () => currentUser?.role === ERole.ADMIN,
    [currentUser?.role]
  );
  const handleSwitch = async (id: string, value: boolean) => {
    try {
      if (value) {
        const res = await runEnable(id as any);
        notification.success({
          message: get(res, "message", "Success!"),
        });
        return;
      }
      const res = await runDisable(id as any);
      notification.success({
        message: get(res, "message", "Success!"),
      });
    } catch (error) {
      notification.error({
        message: get(error, "reason", "Error. Please try again"),
      });
    }
  };
  const columns: any = useMemo(
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
        width: 205,
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
        render: (_: string, record: IUser) => (
          <UserRoleEdit user={record} isAdmin={isAdmin} />
        ),
        filterMultiple: false,
        onFilter: () => {
          return true;
        },
      },
      {
        title: "Enable State",
        dataIndex: "state",
        width: 205,
        filters: [
          {
            text: "Enable",
            value: "ENABLED",
          },
          {
            text: "Disable",
            value: "DISABLED",
          },
        ],
        filterMultiple: false,
        render: (state: string, record: IUser) => (
          <Switch
            loading={pendingEnable || pendingDisable}
            onChange={(e) => handleSwitch(record.id, e)}
            disabled={!isAdmin}
            checked={state === "ENABLED"}
            style={!isAdmin ? { opacity: 0.4 } : {}}
          />
        ),
        onFilter: () => {
          return true;
        },
      },
      {
        title: "Created at",
        dataIndex: "createdAt",
        width: 205,
        render: (createdAt: string) =>
          dayjs(createdAt).format("YYYY-MM-DD HH:mm:ss"),
      },
      {
        title: "Actions",
        dataIndex: "",
        width: 90,
        align: "center",
        render: (record: IUser) => <ResetPwd user={record} />,
        hidden: !isAdmin,
      },
    ],
    [isAdmin, handleSwitch, pendingEnable, pendingDisable]
  );

  useEffect(() => {
    return () => resetParams();
  }, []);

  const handleChange = debounce((e) => {
    setUserParams({
      q: e.target.value,
      page: 0,
    });
  }, 500);

  return (
    <PageLayout title="User management">
      <div className={styles.container}>
        <Flex justify="space-between" align="center">
          <Input.Search
            placeholder="Search user"
            style={{ width: 264 }}
            onChange={handleChange}
          />
          {currentUser?.role === ERole.ADMIN && (
            <Button type="primary" onClick={open}>
              Create new user
            </Button>
          )}
        </Flex>
        <Table
          loading={loadingUser}
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
            onChange: (page, pageSize) => {
              setUserParams({ page: page - 1, size: pageSize });
            },
          }}
          scroll={{
            // y: get(size, "height", 0) - 164,
            x: "auto",
          }}
        />
      </div>

      {isOpen && <UserModal open={isOpen} onClose={close} />}
    </PageLayout>
  );
};

export default UserManagement;
