import { Drawer } from "@/components/Drawer";
import { InfoCircleFilled } from "@ant-design/icons";
import { Alert, DrawerProps, Table } from "antd";
import styles from "./index.module.scss";
import DeploymentStatus from "@/pages/EnvironmentOverview/components/DeploymentStatus";
import { useGetMappingTemplateUpgradeDetail } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { ApiCard } from "@/components/ApiMapping";
import { IDeploymentHistory } from "@/utils/types/product.type";

export function DetailDrawer({ deploymentId, ...props }: Readonly<DrawerProps & { deploymentId: string | null }>) {
  const { currentProduct: productId } = useAppStore();
  const { data, isLoading } = useGetMappingTemplateUpgradeDetail(
    productId,
    deploymentId as any
  );

  return (
    <Drawer
      {...props}
      title="View details"
      width={760}
      maskClosable
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

      <Table<IDeploymentHistory>
        loading={isLoading}
        rowKey={record => record.mapperKey}
        columns={[
          {
            title: "Mapping use case",
            render: (record) => (
              <ApiCard apiInstance={record} mappingMatrixPosition="right" className={styles.noPadding} />
            ),
          },
          {
            title: "Upgrade to",
            dataIndex: "version",
            width: 180,
            render: (version) => <span data-testid="upgradeToVersion">{version}</span>
          },
          {
            title: "Upgrade status",
            width: 180,
            dataIndex: "status",
            render: (status: string) => <DeploymentStatus status={status} />,
          },
        ]}
        dataSource={data}
        pagination={false}
      />
    </Drawer>
  );
}
