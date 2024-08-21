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
import styles from "./index.module.scss";
import { useEffect, useMemo, useState } from "react";
import {
  useDeployProduction,
  useGetAPIDeployments,
  useGetProductEnvs,
  useVerifyProduct,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import EmptyIcon from "@/assets/icon/empty.svg";
import DeployIcon from "@/assets/icon/deploy.svg";
import DetailIcon from "@/assets/icon/detail.svg";
import Text from "@/components/Text";
import dayjs from "dayjs";
import { get, isEmpty } from "lodash";
import DeploymentStatus from "@/pages/EnvironmentOverview/components/DeploymentStatus";
import { useDeploymentStore } from "@/stores/deployment.store";
import { IDeploymentHistory } from "@/utils/types/product.type";
import useUser from "@/hooks/user/useUser";
import InformationModal from "@/components/DeployStage/InformationModal";
import { useBoolean } from "usehooks-ts";
import { IEnv } from "@/utils/types/env.type";
import MappingMatrix from '@/components/MappingMatrix';
import RequestMethod from '@/components/Method';

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
      <Button
        loading={isPending}
        disabled={!record?.verifiedStatus}
        type="text"
        className={styles.defaultBtn}
        onClick={handleClick}
      >
        <DeployIcon />
      </Button>
    </>
  );
};

const DeployHistory = ({ scrollHeight, selectedEnvId }: { scrollHeight?: number, selectedEnvId?: string }) => {
  const { currentProduct } = useAppStore();
  const { data: dataEnv } = useGetProductEnvs(currentProduct);
  const { params, setParams, resetParams } = useDeploymentStore();
  const { data, isLoading } = useGetAPIDeployments(currentProduct, params);
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
    () => [
      selectedEnvId ? {
          title: "API mapping",
          dataIndex: "",
          width: 340,
          render: (item: any) => (
            <Flex gap={10} align="center">
              <RequestMethod method={item?.method} />
              <Typography.Text
                style={{ color: "#2962FF" }}
                ellipsis={{ tooltip: item?.path }}
              >
                {item?.path.split('/').slice(-2).join('/')}
              </Typography.Text>
              <MappingMatrix mappingMatrix={item.mappingMatrix} />
            </Flex>
          ),
      } : {},
      {
        title: "Version",
        dataIndex: "version",
        render: (text: string) => (
          <Flex align="center" gap={8}>
            {text}
          </Flex>
        ),
      },
      {
        title: "Environment",
        dataIndex: "",
        render: (record: IDeploymentHistory) => (
          <div className={styles.capitalize}>{record?.envName}</div>
        ),
        filters: selectedEnvId ? undefined : dataEnv?.data?.map((i) => ({ text: i.name, value: i.id })),
      },
      {
        title: "Deployed by",
        dataIndex: "",
        render: (record: IDeploymentHistory) => (
          <ContentTime content={record?.createBy} time={record?.createAt} />
        ),
      },
      {
        title: "Deploy status",
        dataIndex: "status",
        render: (status: string) => <DeploymentStatus status={status} />,
      },
      {
        title: "Verified for Production",
        dataIndex: "verifiedStatus",
        render: (verifiedStatus: boolean, record: IDeploymentHistory) =>
          record?.envName?.toLowerCase?.() === "stage" && (
            <Switch
              value={verifiedStatus}
              onChange={() => handleVerify(record)}
            />
          ),
      },
      {
        title: "Verified by",
        dataIndex: "",
        render: (record: IDeploymentHistory) =>
          record?.envName?.toLowerCase?.() === "stage" && (
            <ContentTime
              content={record?.verifiedBy}
              time={record?.verifiedAt}
            />
          ),
      },
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
            <Tooltip title="Deploy to Production">
              <DeploymentBtn record={record} env={envItems} />
            </Tooltip>
          </Flex>
        ),
      },
    ].filter(value => Object.keys(value).length !== 0),
    [envItems]
  );
  const onChange: TableProps<any>["onChange"] = (_, filters, __, ___) => {
    const envId: any = get(filters, "1.[0]");
    if (filters[1]?.length === 1) {
      setParams({ envId: envId });
      return;
    }
    setParams({ envId: undefined });
  };

  useEffect(() => {
    if(selectedEnvId) {
      setParams({ envId: selectedEnvId });
    }
  }, [selectedEnvId])

  useEffect(() => {
    return () => {
      resetParams();
    };
  }, []);

  const scroll = scrollHeight ? { y: scrollHeight - 215, x: "max-content" } : undefined

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
        dataSource={get(data, "data", [])?.slice(0, 10)}
        getPopupContainer={() =>
          document.getElementById("deploy-history") as HTMLDivElement
        }
        rowKey="id"
        pagination={{
          onChange: (page, size) => setParams({ page: page - 1, size }),
          pageSize: params.size ?? 10,
          current: (params.page ?? 1) + 1,
          total: get(data, "total", 0),
          showQuickJumper: true,
          showTotal: () => `Total ${get(data, "total", 0)} items`,
        }}
        onChange={onChange}
      />
    </div>
  );
};

export default DeployHistory;
