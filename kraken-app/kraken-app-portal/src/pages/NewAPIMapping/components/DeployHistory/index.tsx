import DetailIcon from "@/assets/icon/detail.svg";
import EmptyIcon from "@/assets/icon/empty.svg";
import MappingMatrix from "@/components/MappingMatrix";
import RequestMethod from "@/components/Method";
import TrimmedPath from "@/components/TrimmedPath";
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
import {
  Button,
  Flex,
  Result,
  Switch,
  Table,
  TableColumnsType,
  TableProps,
  Tooltip,
  Typography,
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
  scrollHeight?: number;
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
      notification.error({ message: get(error, "reason") });
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
        width: 200,
        fixed: "left",
        render: (item: any) => (
          <Flex gap={10} align="center">
            <RequestMethod method={item?.method} />
            <Typography.Text
              style={{ color: "#2962FF" }}
              ellipsis={{ tooltip: item?.path }}
            >
              <TrimmedPath trimLevel={2} path={item?.path} />
            </Typography.Text>
            <MappingMatrix mappingMatrix={item.mappingMatrix} />
          </Flex>
        ),
      });

    columns.push(
      {
        title: "Version",
        dataIndex: "version",
        fixed: !selectedEnv && "left",
        width: 90,
        render: (text: string) => (
          <Flex align="center" gap={8}>
            {text}
          </Flex>
        ),
      },
      {
        title: "Environment",
        key: "environment",
        width: 200,
        render: (record: IDeploymentHistory) => (
          <div className={styles.capitalize}>{record?.envName}</div>
        ),
        filters: selectedEnv
          ? undefined
          : dataEnv?.data?.map((i) => ({ text: i.name, value: i.id })),
      },
      {
        title: "Deployed by",
        width: 200,
        render: (record: IDeploymentHistory) => (
          <ContentTime content={record?.createBy} time={record?.createAt} />
        ),
      },
      {
        title: "Deploy status",
        dataIndex: "status",
        width: 200,
        render: (status: string) => <DeploymentStatus status={status} />,
      }
    );

    if (isStage)
      columns.push(
        {
          title: "Verified for Production",
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
          width: 200,
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
      width: 120,
      fixed: "right",
      render: (record: IDeploymentHistory) => (
        <Flex gap={12} align="center">
          <Tooltip title="Check details and difference">
            <Button type="text" className={styles.defaultBtn}>
              <DetailIcon />
            </Button>
          </Tooltip>
          {record.envName !== "production" && (
            <DeploymentBtn record={record} env={envItems} />
          )}
        </Flex>
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

  const scroll = scrollHeight
    ? { y: scrollHeight - 215, x: "max-content" }
    : undefined;

  return (
    <div className={styles.root} id="deploy-history">
      <Table
        scroll={scroll}
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
