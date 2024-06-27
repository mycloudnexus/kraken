import Text from "@/components/Text";
import dayjs from "dayjs";
import {
  useGetProductDeployments,
  useGetProductEnvs,
  useGetAllApiKeyList,
  useGetAllDataPlaneList,
  useGetRunningComponentList,
  useCreateApiKey,
} from "@/hooks/product";
import { useCommonListProps } from "@/hooks/useCommonListProps";
import { toDateTime, toTime } from "@/libs/dayjs";
import { useAppStore } from "@/stores/app.store";
import { ROUTES } from "@/utils/constants/route";
import { IEnvComponent } from "@/utils/types/envComponent.type";
import { MoreOutlined } from "@ant-design/icons";
import {
  Button,
  Dropdown,
  Flex,
  Form,
  Input,
  MenuProps,
  Row,
  Select,
  Spin,
  Table,
  Tag,
  Col,
  notification,
  Typography,
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

const initPagination = {
  pageSize: 5,
  current: 0,
};
const initPaginationParams = {
  size: 20,
  page: 0,
};

const EnvironmentOverview = () => {
  const navigate = useNavigate();
  const { currentProduct } = useAppStore();
  const { data: envs, isLoading: loadingEnvs } =
    useGetProductEnvs(currentProduct);

  const { data: apiKey } = useGetAllApiKeyList(
    currentProduct,
    initPaginationParams
  );

  const { data: dataPlane } = useGetAllDataPlaneList(
    currentProduct,
    initPaginationParams
  );

  const { data: runningComponent } = useGetRunningComponentList(currentProduct);

  const { mutateAsync: createApiKeyMutate } = useCreateApiKey();
  const [open, setOpen] = useState(false);
  const [currentEnvId, setCurrentEnvId] = useState<string | undefined>();
  const modalConfirmRef = useRef<any>();

  const generateApiKey = useCallback(
    async (envId: string, evName: string, closeConfirm = false) => {
      const name = `${evName}_${dayjs.utc().format("YYYY-MM-DD HH:mm:ss")}`;
      try {
        const res = await createApiKeyMutate({
          productId: currentProduct,
          envId,
          name,
        } as any);

        closeConfirm && modalConfirmRef?.current?.destroy();
        showModalShowNew(res?.data?.token);
      } catch (e: any) {
        notification.error({ message: e?.data?.error || "generate failed" });
      }
    },
    [currentProduct]
  );
  const onConfirmRotate = (id: string, name: string) => () => {
    return generateApiKey(id, name, true);
  };

  const dropdownItems: (
    envId: string,
    envName: string,
    len: number
  ) => MenuProps["items"] = useCallback(
    (envId, envName, len = 0) => [
      {
        key: "view-log",
        label: "API activity log",
        onClick: () => {
          navigate(ROUTES.ENV_ACTIVITY_LOG(envId));
        },
      },

      {
        key: "refresh-key",
        label: "Rotate API key",
        onClick: () => {
          modalConfirmRef.current = showModalConfirmRotate(
            envName,
            onConfirmRotate(envId, envName)
          );
        },
      },
      {
        key: "create",
        label: "Create new deployment",
        onClick: () => {
          setCurrentEnvId(envId);
          setOpen(true);
        },
        disabled: len === 0,
      },
    ],
    []
  );

  const getDataPlaneInfo = useCallback(
    (id: string) => {
      if (!dataPlane?.data) return {};
      const list = dataPlane.data.filter((i) => i.envId === id);
      const status = list.every((n) => n.status === "OK");
      const len = list.length;
      const disConnectNum = list.filter((n) => n.status !== "OK")?.length;
      const connectNum = list.filter((n) => n.status === "OK")?.length;
      return { len, status, disConnectNum, connectNum };
    },
    [dataPlane]
  );
  const getRunningList = useCallback(
    (id: string) => {
      if (!runningComponent?.data) return [];
      const list = runningComponent.data.find((i) => i.id === id)?.components;

      return list;
    },
    [runningComponent]
  );

  const getLastDeploymentTime = useCallback(
    (id: any) => {
      if (!runningComponent?.data) return [];
      const target = runningComponent?.data.find((i) => i.id === id);

      if (!target?.createdAt) return "-";
      return `${toDateTime(target.createdAt, true)} | ${toTime(
        target.createdAt
      )}`;
    },
    [runningComponent?.data]
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
      history: true,
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
      render: (h) => h.name,
    },
    {
      key: "components",
      title: "Component",
      render: (h) => (
        <Flex vertical gap={20}>
          {h.components.map((component: any) => (
            <Flex gap={4} align="center" key={component.id}>
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
      title: "Deployed time",
      render: (h) => toDateTime(h.createdAt),
    },
  ];
  return (
    <Flex vertical gap={12} className={styles.pageWrapper}>
      <Flex vertical gap={14} className={styles.sectionWrapper}>
        <Flex justify="space-between" align="center">
          <Text.BoldLarge>Environment Overview</Text.BoldLarge>
        </Flex>
        <Spin spinning={loadingEnvs}>
          <div className={styles.overviewContainer}>
            {envs?.data.map((env) => {
              const haveApiKey = !!apiKey?.data.find((i) => i.envId === env.id);
              const {
                disConnectNum,
                connectNum,
                len = 0,
              } = getDataPlaneInfo(env.id);
              return (
                <Flex
                  vertical
                  gap={16}
                  key={env.id}
                  className={styles.overviewItem}
                  justify="space-between"
                >
                  <div style={{ width: "100%" }}>
                    <Flex justify="space-between" style={{ marginBottom: 16 }}>
                      <Text.BoldMedium>{env.name}</Text.BoldMedium>
                      <Dropdown
                        disabled={!haveApiKey}
                        menu={{
                          items: dropdownItems(env.id, env.name, len),
                        }}
                      >
                        <MoreOutlined
                          style={{
                            cursor: haveApiKey ? "default" : "not-allowed",
                          }}
                        />
                      </Dropdown>
                    </Flex>
                    <EnvStatus
                      apiKey={haveApiKey}
                      status={getDataPlaneInfo(env.id)?.status}
                      disConnect={disConnectNum}
                      connect={connectNum}
                      dataPlane={len}
                    />
                    <div className={styles.runningContainer}>
                      {getRunningList(env.id)?.map((r) => (
                        <Row justify={"space-between"} key={r.id}>
                          <Typography.Text
                            className={styles.running}
                            ellipsis={{ tooltip: true }}
                          >
                            {r.componentName}
                          </Typography.Text>

                          <Col>{r.version}</Col>
                        </Row>
                      ))}
                    </div>
                  </div>

                  {haveApiKey ? (
                    <Flex
                      justify="space-between"
                      align="center"
                      className={styles.lastDeloyed}
                    >
                      <Text.NormalSmall style={{ color: "#00000073" }}>
                        Last Deployed at
                      </Text.NormalSmall>
                      <Text.NormalSmall style={{ color: "#00000073" }}>
                        {getLastDeploymentTime(env.id)}
                      </Text.NormalSmall>
                    </Flex>
                  ) : (
                    <Button
                      type="primary"
                      onClick={() => {
                        generateApiKey(env.id, env.name);
                      }}
                    >
                      Create API Key
                    </Button>
                  )}
                </Flex>
              );
            })}
          </div>
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
          scroll={{ x: 600 }}
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

      <ModalNewDeployment
        open={open}
        setOpen={setOpen}
        runningComponent={runningComponent}
        currentEnvId={currentEnvId!}
      />
    </Flex>
  );
};

export default EnvironmentOverview;
