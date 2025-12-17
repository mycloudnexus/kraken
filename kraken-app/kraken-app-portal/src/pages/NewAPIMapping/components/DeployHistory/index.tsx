import EmptyIcon from "@/assets/icon/empty.svg";
import { ApiCard } from "@/components/ApiMapping";
import {
  PRODUCT_CACHE_KEYS,
  useGetAPIDeployments,
  useGetProductEnvs,
  useVerifyProduct,
} from "@/hooks/product";
import DeploymentStatus from "@/pages/EnvironmentOverview/components/DeploymentStatus";
import { useAppStore } from "@/stores/app.store";
import { DEFAULT_PAGING } from "@/utils/constants/common";
import { queryClient } from "@/utils/helpers/reactQuery";
import { IPagination } from "@/utils/types/common.type";
import { IEnv } from "@/utils/types/env.type";
import { IDeploymentHistory } from "@/utils/types/product.type";
import { InfoCircleOutlined } from "@ant-design/icons";
import {
  Flex,
  Result,
  Switch,
  Table,
  TableColumnsType,
  TableProps,
  Tooltip,
  notification,
} from "antd";
import { get, omit } from "lodash";
import { useEffect, useMemo, useState } from "react";
import { ContentTime } from "./ContentTime";
import { DeploymentBtn } from "./DeployBtn";
import styles from "./index.module.scss";

const DeployHistory = ({
  scrollHeight,
  selectedEnv,
  targetMapperKey,
}: {
  scrollHeight: number;
  selectedEnv?: IEnv;
  targetMapperKey?: string;
}) => {
  const { currentProduct } = useAppStore();
  const { data: dataEnv } = useGetProductEnvs(currentProduct);

  const [query, setQuery] = useState<IPagination & { envId?: string }>({
    page: DEFAULT_PAGING.page,
    size: DEFAULT_PAGING.size,
    total: 0,
    envId: undefined,
  });

  const { data, isLoading, isFetching } = useGetAPIDeployments(currentProduct, {
    mapperKey: targetMapperKey,
    ...omit(query, "total"),
    envId: selectedEnv?.id ?? query.envId,
  });

  const { mutateAsync: verify, isPending: isLoadingVerify } =
    useVerifyProduct();

  const envItems = useMemo(() => {
    const stage = dataEnv?.data?.find(
      (e: IEnv) => e.name?.toLowerCase() === "stage"
    );
    const prod = dataEnv?.data?.find(
      (e: IEnv) => e.name?.toLowerCase() === "production"
    );
    return {
      stageId: get(stage, "id", ""),
      productionId: get(prod, "id", ""),
    };
  }, [dataEnv]);

  const handleVerify = async (record: IDeploymentHistory) => {
    try {
      const body: any = {
        productId: currentProduct,
        data: {
          tagId: record?.tagId,
          verified: !record?.verifiedStatus,
        },
      };
      const res = await verify(body);
      if (res) {
        notification.success({ message: get(res, "message") });
      }
    } catch (error) {
      const status = get(error, "response.status");
      let errorMessage = get(error, "reason") ?? '';
      if (status === 403) {
        errorMessage = "You do not have permission to perform the 'Verify' action";
      }
      notification.error({ message: errorMessage || 'An unexpected error occurred during verification.'  });
    }
  };

  const isStage = useMemo(
    () => selectedEnv?.name?.toLowerCase() !== "production",
    [selectedEnv?.name]
  );

  const columns = useMemo(() => {
    const columns: TableColumnsType<any> = [];
    if (selectedEnv)
      columns.push({
        title: "API mapping",
        width: 400,
        fixed: "left",
        render: (item: any) => <ApiCard apiInstance={item} />,
      });

    columns.push(
      {
        title: "Version",
        dataIndex: "version",
        fixed: !selectedEnv && "left",
        width: 80,
        render: (text: string) => (
          <Flex align="center" gap={8}>
            {text}
          </Flex>
        ),
      },
      {
        title: "Environment",
        key: "environment",
        width: 120,
        render: (record: IDeploymentHistory) => (
          <div className={styles.capitalize}>{record?.envName}</div>
        ),
        filters: selectedEnv
          ? undefined
          : dataEnv?.data?.map((i) => ({ text: i.name, value: i.id })),
      },
      {
        title: "Deployed by",
        width: 120,
        render: (record: IDeploymentHistory) => (
          <ContentTime content={record?.createBy} time={record?.createAt} />
        ),
      },
      {
        title: "Deploy status",
        dataIndex: "status",
        width: 100,
        render: (status: string) => <DeploymentStatus status={status} />,
      }
    );

    if (isStage)
      columns.push(
        {
          title: (
            <>
              Verified for Production{" "}
              <Tooltip title="Toggle this button means you have verified this deployment version in stage environment">
                <InfoCircleOutlined />
              </Tooltip>
            </>
          ),
          dataIndex: "verifiedStatus",
          width: 160,
          render: (verifiedStatus: boolean, record: IDeploymentHistory) =>
            record?.envName?.toLowerCase?.() === "stage" && (
              <Switch
                value={verifiedStatus}
                disabled={
                  record?.status?.toLowerCase?.() === "failed" ||
                  record.status === "in progress"
                }
                onChange={() => handleVerify(record)}
              />
            ),
        },
        {
          title: "Verified by",
          width: 120,
          render: (record: IDeploymentHistory) =>
            record?.envName?.toLowerCase?.() === "stage" && (
              <ContentTime
                content={record?.verifiedBy}
                time={record?.verifiedAt}
              />
            ),
        }
      );

    columns.push({
      title: "Actions",
      width: 80,
      fixed: "right",
      render: (record: IDeploymentHistory) => (
        <>
          {record.envName !== "production" && (
            <DeploymentBtn record={record} env={envItems} />
          )}
        </>
      ),
    });

    return columns;
  }, [envItems, selectedEnv]);

  const onChange: TableProps<any>["onChange"] = (_, filters) => {
    const envIds: any = filters.environment as string[];

    setQuery((prevQuery) => ({
      ...prevQuery,
      page: envIds ? 0 : prevQuery.page,
      envId: envIds ? envIds.join(",") : undefined,
    }));
  };

  useEffect(() => {
    if (data?.total !== undefined) {
      setQuery((prevQuery) => ({
        ...prevQuery,
        total: data.total,
      }));
    }
  }, [data?.total, setQuery]);

  useEffect(() => {
    // Making sure deployment version & status are in sync
    queryClient.invalidateQueries({
      queryKey: [PRODUCT_CACHE_KEYS.get_running_api_list],
    });
  }, []);

  return (
    <div className={styles.root} id="deploy-history">
      <Table
        scroll={{ y: scrollHeight - 144, x: 800 }}
        loading={isLoading || isFetching || isLoadingVerify}
        locale={{
          emptyText: (
            <Result subTitle="No deploy history" icon={<EmptyIcon />} />
          ),
        }}
        columns={columns}
        dataSource={data?.data ?? []}
        getPopupContainer={() =>
          document.getElementById("deploy-history") as HTMLDivElement
        }
        rowKey="id"
        tableLayout="fixed"
        pagination={{
          current: query.page + 1,
          pageSize: query.size,
          total: query?.total || 0,
          showSizeChanger: true,
          showTotal: (total) => `Total ${total} items`,
          onChange: (current, pageSize) =>
            setQuery((prevQuery) => ({
              ...prevQuery,
              page: current - 1,
              size: pageSize,
            })),
        }}
        onChange={onChange}
      />
    </div>
  );
};

export default DeployHistory;
