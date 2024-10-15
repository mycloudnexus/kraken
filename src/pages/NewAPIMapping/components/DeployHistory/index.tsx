import DeployIcon from "@/assets/icon/deploy.svg";
import DetailIcon from "@/assets/icon/detail.svg";
import EmptyIcon from "@/assets/icon/empty.svg";
import InformationModal from "@/components/DeployStage/InformationModal";
import MappingMatrix from "@/components/MappingMatrix";
import RequestMethod from "@/components/Method";
import Text from "@/components/Text";
import TrimmedPath from "@/components/TrimmedPath";
import {
  useDeployProduction,
  useGetAPIDeployments,
  useGetProductEnvs,
  useVerifyProduct,
} from "@/hooks/product";
import { useCommonListProps } from "@/hooks/useCommonListProps";
import useUser from "@/hooks/user/useUser";
import DeploymentStatus from "@/pages/EnvironmentOverview/components/DeploymentStatus";
import { useAppStore } from "@/stores/app.store";
import { DEFAULT_PAGING } from "@/utils/constants/common";
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
import dayjs from "dayjs";
import { get, isEmpty, omit } from "lodash";
import { useEffect, useMemo, useState } from "react";
import { useBoolean } from "usehooks-ts";
import styles from "./index.module.scss";

const initPagination = {
  pageSize: DEFAULT_PAGING.size,
  current: DEFAULT_PAGING.page,
};

export const ContentTime = ({ content = "", time = "" }) => {
  const { findUserName } = useUser();
  const userName = useMemo(() => findUserName(content), [findUserName]);
  return (
    <Flex vertical gap={2}>
      <Text.LightMedium>{userName}</Text.LightMedium>
      {!isEmpty(time) && (
        <Text.LightSmall color="#00000073">
          {dayjs.utc(time).local().format("YYYY-MM-DD HH:mm:ss")}
        </Text.LightSmall>
      )}
    </Flex>
  );
};

export const DeploymentBtn = ({
  record,
  env,
}: {
  record: IDeploymentHistory;
  env: Record<string, string>;
}) => {
  const { currentProduct } = useAppStore();
  const { value: isOpen, setTrue: open, setFalse: close } = useBoolean(false);
  const [modalText, setModalText] = useState("");
  const { mutateAsync: runDeploy, isPending } = useDeployProduction();
  const handleClick = async () => {
    try {
      const res = await runDeploy({
        productId: currentProduct,
        data: {
          tagInfos: [
            {
              tagId: record?.tagId,
            },
          ],
          sourceEnvId: env?.stageId,
          targetEnvId: env?.productionId,
        },
      } as any);

      notification.success({ message: get(res, "message", "Success!") });
    } catch (error) {
      setModalText(get(error, "reason", "Error. Please try again"));
      open();
    }
  };
  return (
    <>
      <InformationModal modalText={modalText} open={isOpen} onClose={close} />
      <Tooltip title="Deploy to Production">
        <Button
          loading={isPending}
          disabled={!record?.verifiedStatus}
          type="text"
          className={styles.defaultBtn}
          onClick={handleClick}
        >
          <DeployIcon />
        </Button>
      </Tooltip>
    </>
  );
};

const DeployHistory = ({
  scrollHeight,
  selectedEnv,
  targetMapperKey,
}: {
  scrollHeight?: number;
  selectedEnv?: IEnv;
  targetMapperKey?: string;
}) => {
  const {
    tableData,
    queryParams,
    setQueryParams,
    pagination,
    setPagination,
    setTableData,
    handlePaginationChange,
    handlePaginationShowSizeChange,
  } = useCommonListProps({}, initPagination);

  const { currentProduct } = useAppStore();
  const { data: dataEnv } = useGetProductEnvs(currentProduct);
  const { data, isLoading } = useGetAPIDeployments(currentProduct, {
    mapperKey: targetMapperKey,
    ...omit(queryParams, ["envId"]),
    envId: selectedEnv?.id,
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

  const columns: TableColumnsType<any> = useMemo(
    () =>
      [
        selectedEnv
          ? {
              title: "API mapping",
              dataIndex: "",
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
            }
          : {},
        {
          title: "Version",
          dataIndex: "version",
          width: 90,
          render: (text: string) => (
            <Flex align="center" gap={8}>
              {text}
            </Flex>
          ),
        },
        {
          title: "Environment",
          dataIndex: "",
          width: selectedEnv ? 120 : undefined,
          render: (record: IDeploymentHistory) => (
            <div className={styles.capitalize}>{record?.envName}</div>
          ),
          filters: selectedEnv
            ? undefined
            : dataEnv?.data?.map((i) => ({ text: i.name, value: i.id })),
        },
        {
          title: "Deployed by",
          width: selectedEnv ? 150 : undefined,
          dataIndex: "",
          render: (record: IDeploymentHistory) => (
            <ContentTime content={record?.createBy} time={record?.createAt} />
          ),
        },
        {
          title: "Deploy status",
          dataIndex: "status",
          width: selectedEnv ? 120 : undefined,
          render: (status: string) => <DeploymentStatus status={status} />,
        },
        selectedEnv?.name?.toLowerCase() !== "production"
          ? {
              title: "Verified for Production",
              dataIndex: "verifiedStatus",
              width: selectedEnv ? 180 : 140,
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
            }
          : {},
        selectedEnv?.name?.toLowerCase() !== "production"
          ? {
              title: "Verified by",
              dataIndex: "",
              width: selectedEnv ? 130 : undefined,
              render: (record: IDeploymentHistory) =>
                record?.envName?.toLowerCase?.() === "stage" && (
                  <ContentTime
                    content={record?.verifiedBy}
                    time={record?.verifiedAt}
                  />
                ),
            }
          : {},
        {
          title: "Actions",
          dataIndex: "",
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
        },
      ].filter((value) => Object.keys(value).length !== 0),
    [envItems, selectedEnv]
  );

  const onChange: TableProps<any>["onChange"] = (pagination, filters) => {
    const envId: any = get(filters, "1.[0]");
    if (filters[1]?.length === 1) {
      setQueryParams({ envId: envId });
      return;
    }
    setQueryParams({
      envId: undefined,
      page: Number(pagination?.current) - 1,
      size: pagination.pageSize,
    });
  };

  useEffect(() => {
    if (!isLoading) {
      const updatedTableData = data?.data;
      const updatedPagination = {
        current: data?.page ?? initPagination.current,
        pageSize: data?.size ?? initPagination.pageSize,
        total: data?.total ?? 0,
      };
      setPagination(updatedPagination);
      setTableData(updatedTableData);
    }
  }, [data, isLoading]);

  useEffect(() => {
    if (selectedEnv) {
      setQueryParams({ envId: selectedEnv });
    }
  }, [selectedEnv]);

  useEffect(() => {
    return () => {
      setQueryParams({ ...initPagination });
    };
  }, []);

  const scroll = scrollHeight
    ? { y: scrollHeight - 215, x: "max-content" }
    : undefined;

  return (
    <div className={styles.root} id="deploy-history">
      <Table
        scroll={scroll}
        loading={isLoading || isLoadingVerify}
        locale={{
          emptyText: (
            <Result subTitle="No deploy history" icon={<EmptyIcon />} />
          ),
        }}
        columns={columns}
        dataSource={tableData}
        getPopupContainer={() =>
          document.getElementById("deploy-history") as HTMLDivElement
        }
        rowKey="id"
        pagination={{
          pageSize: pagination.pageSize,
          current: pagination.current + 1,
          onChange: handlePaginationChange,
          total: pagination?.total || 0,
          showSizeChanger: true,
          onShowSizeChange: handlePaginationShowSizeChange,
          showTotal: (total) => `Total ${total} items`,
        }}
        onChange={onChange}
      />
    </div>
  );
};

export default DeployHistory;
