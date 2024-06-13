import ComponentIcon from "@/assets/component.svg";
import Text from "@/components/Text";
import { useGetProductDeployments, useGetProductEnvs } from "@/hooks/product";
import { useCommonListProps } from "@/hooks/useCommonListProps";
import { toDateTime, toTime } from "@/libs/dayjs";
import { useAppStore } from "@/stores/app.store";
import { ROUTES } from "@/utils/constants/route";
import { IEnvComponent } from "@/utils/types/envComponent.type";
import { MoreOutlined, PlusOutlined } from "@ant-design/icons";
import {
  Button,
  Dropdown,
  Flex,
  Form,
  Input,
  MenuProps,
  Select,
  Spin,
  Table,
  Tag,
} from "antd";
import { ColumnsType } from "antd/es/table";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import DeploymentStatus from "./components/DeploymentStatus";
import EnvStatus from "./components/EnvStatus";
import { showModalConfirmRotate } from "./components/ModalConfirmRotateAPIKey";
import ModalNewDeployment from "./components/ModalNewDeployment";
import { showModalShowNew } from "./components/ModalShowAPIKey";
import styles from "./index.module.scss";

const availableEnvs = ["dev", "production", "uat", "stage"];
const initPagination = {
  pageSize: 5,
  current: 0,
};

const EnvironmentOverview = () => {
  const navigate = useNavigate();
  const { currentProduct } = useAppStore();
  const { data: envs, isLoading: loadingEnvs } =
    useGetProductEnvs(currentProduct);
  const [open, setOpen] = useState(false);
  const modalConfirmRef = useRef<any>();
  const onConfirmRotate = () => () => {
    modalConfirmRef?.current?.destroy();
    showModalShowNew(
      "abcdefefacnakcnabcdefefacnakcnabcdefefacnakcnabcdefefacnakcnabcdefefacnakcnabcdefefacnakcn"
    );
  };
  const dropdownItems: (envId: string, envName: string) => MenuProps["items"] =
    useCallback(
      (envId, envName) => [
        {
          key: "view-log",
          label: "API activity log",
          onClick: () => {
            navigate(ROUTES.ENV_ACTIVITY_LOG(envId));
          },
        },
        {
          key: "view-details",
          label: "View details",
          children: [
            {
              key: "address-validate",
              label: "Address Validation",
            },
            {
              key: "quality",
              label: "Product offering quality",
            },
            {
              key: "quote",
              label: "Quote",
            },
            {
              key: "order",
              label: "Order",
            },
          ],
        },
        {
          key: "refresh-key",
          label: "Rotate API key",
          onClick: () => {
            modalConfirmRef.current = showModalConfirmRotate(
              envName,
              onConfirmRotate()
            );
          },
        },
      ],
      []
    );

  const {
    tableData,
    queryParams,
    setQueryParams,
    pagination,
    setPagination,
    setTableData,
    handlePaginationChange,
    handlePaginationShowSizeChange,
  } = useCommonListProps(
    {
      history: false,
    },
    initPagination
  );

  const { data, isLoading } = useGetProductDeployments(
    currentProduct,
    queryParams
  );

  useEffect(() => {
    if (!isLoading) {
      const updatedTableData = data?.data;
      const updatedPagination = {
        current: data?.page ?? initPagination.current,
        pageSize: data?.size ?? initPagination.pageSize,
        total: 1,
      };
      setPagination(updatedPagination);
      setTableData(updatedTableData!);
    }
  }, [data, isLoading]);

  const handleFormValuesChange = (_: any, values: any) => {
    setQueryParams(values);
  };
  const columns: ColumnsType<IEnvComponent> = [
    {
      key: "env",
      title: "Environment",
      render: (h) => <Tag bordered={false}>{h.name}</Tag>,
    },
    {
      key: "components",
      title: "Component",
      render: (h) => (
        <Flex vertical gap={20}>
          {h.components.map((component: any) => (
            <Flex gap={4} align="center" key={component.id}>
              <ComponentIcon />
              {component.componentName}
            </Flex>
          ))}
        </Flex>
      ),
    },
    {
      key: "version",
      title: "Version",
      render: (h) => (
        <Flex vertical gap={20} align="flex-start">
          {h.components.map((component: any) => (
            <Tag bordered={false} key={component.id}>
              {component.version}
            </Tag>
          ))}
        </Flex>
      ),
    },
    {
      key: "status",
      title: "Status",
      render: (h) => <DeploymentStatus status={h.status} />,
    },
    {
      key: "createdAt",
      title: "Timestamp",
      render: (h) => toDateTime(h.createdAt),
    },
    {
      key: "action",
      title: "Action",
      render: () => <>Version detail | Mapping | View details</>,
    },
  ];
  return (
    <Flex vertical gap={12} className={styles.pageWrapper}>
      <Flex vertical gap={14} className={styles.sectionWrapper}>
        <Flex justify="space-between" align="center">
          <Text.BoldLarge>Environment Overview</Text.BoldLarge>
          <Button type="primary" onClick={() => setOpen(true)}>
            <PlusOutlined /> Add deployment
          </Button>
        </Flex>
        <Spin spinning={loadingEnvs}>
          <Flex gap={36}>
            {availableEnvs.map((env) => {
              const envData = envs?.data?.find((item) => item.name === env);
              return (
                <Flex
                  vertical
                  gap={16}
                  key={env}
                  className={styles.overviewItem}
                >
                  <Flex justify="space-between" align="center">
                    <Text.BoldMedium>{env}</Text.BoldMedium>
                    {envData && (
                      <Dropdown
                        menu={{
                          items: dropdownItems(envData.id, envData.name),
                        }}
                      >
                        <MoreOutlined />
                      </Dropdown>
                    )}
                  </Flex>
                  <EnvStatus
                    apiKey={undefined}
                    status={undefined}
                    dataPlane={undefined}
                  />
                  {envData?.createdAt && (
                    <Flex
                      justify="space-between"
                      align="center"
                      className={styles.lastDeloyed}
                    >
                      <Text.NormalSmall style={{ color: "#00000073" }}>
                        Last Deployed at
                      </Text.NormalSmall>
                      <Text.NormalSmall style={{ color: "#00000073" }}>
                        {toDateTime(envData.createdAt, true)} |{" "}
                        {toTime(envData.createdAt)}
                      </Text.NormalSmall>
                    </Flex>
                  )}
                </Flex>
              );
            })}
          </Flex>
        </Spin>
      </Flex>
      <Flex
        vertical
        gap={8}
        className={styles.sectionWrapper}
        style={{ flex: 1 }}
      >
        <Text.BoldLarge>Deployment history</Text.BoldLarge>
        <Flex align="center">
          <Form
            layout="inline"
            colon={false}
            onValuesChange={handleFormValuesChange}
          >
            <Form.Item>
              <Input.Search />
            </Form.Item>
            <Form.Item label="Environment" name="envId">
              <Select
                options={[]}
                popupMatchSelectWidth={false}
                placeholder="All"
              />
            </Form.Item>
            <Form.Item label="Status" name="status">
              <Select
                options={[]}
                popupMatchSelectWidth={false}
                placeholder="All"
              />
            </Form.Item>
          </Form>
        </Flex>
        <Table
          dataSource={tableData}
          columns={columns}
          rowKey="id"
          loading={isLoading}
          className={styles.table}
          rowClassName={styles.hovering}
          pagination={{
            hideOnSinglePage: true,
            pageSize: pagination.pageSize,
            current: pagination.current + 1,
            onChange: handlePaginationChange,
            total: pagination.total,
            showSizeChanger: true,
            onShowSizeChange: handlePaginationShowSizeChange,
            showTotal: (total) => `Total ${total} items`,
          }}
        />
      </Flex>
      <ModalNewDeployment open={open} setOpen={setOpen} />
    </Flex>
  );
};

export default EnvironmentOverview;
