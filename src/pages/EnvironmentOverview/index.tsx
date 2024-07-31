import Text from "@/components/Text";
import dayjs from "dayjs";
import {
  useGetProductEnvs,
  useGetAllApiKeyList,
  useGetAllDataPlaneList,
  useGetRunningComponentList,
  useCreateApiKey,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { ROUTES } from "@/utils/constants/route";
import { MoreOutlined } from "@ant-design/icons";
import { Button, Dropdown, Flex, MenuProps, Spin, notification } from "antd";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import EnvStatus from "./components/EnvStatus";
import { showModalConfirmRotate } from "./components/ModalConfirmRotateAPIKey";
import ModalNewDeployment from "./components/ModalNewDeployment";
import { showModalShowNew } from "./components/ModalShowAPIKey";
import styles from "./index.module.scss";
import { isEmpty, sortBy } from "lodash";
import clsx from "clsx";
import RunningAPIMapping from "./components/RunningAPIMapping";
import DeploymentHistory from "./components/DeploymentHistory";
import { IEnv } from "@/utils/types/env.type";

const initPaginationParams = {
  page: 0,
  size: 20,
};

const EnvironmentOverview = () => {
  const [searchParams] = useSearchParams();
  const envId = searchParams.get("envId") || "";
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
  const [selectedEnv, setSelectedEnv] = useState<IEnv | undefined>();

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
    (envId, envName) => [
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

  const envList = useMemo(() => {
    return sortBy(envs?.data, "name", []).reverse();
  }, [envs]);

  useEffect(() => {
    if (envList && isEmpty(selectedEnv?.id) && !envId) {
      setSelectedEnv(envList[0]);
    }
    if (!!envId && !isEmpty(envs)) {
      const env = envList?.find((i) => i.id === envId);
      setSelectedEnv(env);
    }
  }, [envList, envId]);

  return (
    <Flex vertical gap={12} className={styles.pageWrapper}>
      <Flex vertical gap={14}>
        <Spin spinning={loadingEnvs}>
          <div className={styles.overviewContainer}>
            {envList?.map((env) => {
              const haveApiKey = !!apiKey?.data.find((i) => i.envId === env.id);
              const {
                disConnectNum,
                connectNum,
                len = 0,
              } = getDataPlaneInfo(env.id);
              return (
                <div
                  key={env.id}
                  className={clsx(
                    styles.overviewItem,
                    selectedEnv?.id === env.id && styles.overviewItemActive
                  )}
                  role="none"
                  onClick={() => setSelectedEnv(env)}
                >
                  <div style={{ width: "100%" }}>
                    <Flex justify="flex-start" gap={12} align="center">
                      <Text.NormalLarge
                        style={{ marginRight: 16, textTransform: "capitalize" }}
                      >
                        {env.name}
                      </Text.NormalLarge>
                      <EnvStatus
                        apiKey={haveApiKey}
                        status={getDataPlaneInfo(env.id)?.status}
                        disConnect={disConnectNum}
                        connect={connectNum}
                        dataPlane={len}
                      />
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
                  </div>
                </div>
              );
            })}
          </div>
        </Spin>
      </Flex>
      <Flex vertical gap={12} className={styles.sectionWrapper}>
        <Flex align="center" justify="space-between">
          <Text.NormalLarge>
            {selectedEnv?.name?.toLocaleLowerCase?.() === "production"
              ? "Running Component Versions"
              : "Running API Mappings"}
          </Text.NormalLarge>
          {selectedEnv?.name?.toLocaleLowerCase?.() === "production" && (
            <Button
              type="primary"
              onClick={() => {
                setCurrentEnvId(selectedEnv?.id);
                setOpen(true);
              }}
            >
              Create new deployment
            </Button>
          )}
        </Flex>
        <RunningAPIMapping env={selectedEnv} />
      </Flex>
      <Flex
        vertical
        gap={8}
        className={styles.sectionWrapper}
        style={{ flex: 1 }}
      >
        <Text.NormalLarge>
          {selectedEnv?.name?.toLocaleLowerCase?.() === "production"
            ? "Component deployment history"
            : "API Mapping Deployment history"}
        </Text.NormalLarge>
        <DeploymentHistory env={selectedEnv} />
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
