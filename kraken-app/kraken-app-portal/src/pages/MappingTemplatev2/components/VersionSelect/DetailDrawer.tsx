import { Drawer } from "@/components/Drawer";
import { InfoCircleFilled } from "@ant-design/icons";
import { Alert, DrawerProps, Table } from "antd";
import styles from "./index.module.scss";

export function DetailDrawer(props: Readonly<DrawerProps>) {
  return (
    <Drawer
      {...props}
      title="View details"
      width={615}
      className={styles.detailDrawer}
    >
      <Alert
        data-testid="notification"
        type="info"
        description={
          <>
            <InfoCircleFilled />
            <span>
              Following mapping use cases upgraded because of this template
              upgrade.
            </span>
          </>
        }
      />

      <Table
        columns={[
          {
            title: "Mapping use case",
          },
          {
            title: "Upgrade to",
          },
          {
            title: "Upgrade status",
          },
        ]}
        dataSource={[]}
        pagination={false}
      />
    </Drawer>
  );
}
