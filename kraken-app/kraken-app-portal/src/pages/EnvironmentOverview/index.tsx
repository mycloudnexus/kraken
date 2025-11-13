import { PageLayout } from "@/components/Layout";
import {
  useGetProductEnvs,
  useGetAllApiKeyList,
  useGetAllDataPlaneList,
  useGetRunningComponentList,
  useCreateApiKey,
  useRotateApiKey,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IEnv } from "@/utils/types/env.type";
import { MoreOutlined } from "@ant-design/icons";
import { Dropdown, Flex, Radio, Spin, Typography, notification } from "antd";
import clsx from "clsx";
import dayjs from "dayjs";
import { every, isEmpty, sortBy } from "lodash";
import {
  useState,
  useCallback,
  useMemo,
  useEffect,
  useRef,
  lazy,
  Suspense,
} from "react";
import { useSearchParams } from "react-router-dom";
import EnvStatus from "./components/EnvStatus";
import { showModalConfirmRotate } from "./components/ModalConfirmRotateAPIKey";
import ModalNewDeployment from "./components/ModalNewDeployment";
import { showModalShowNew } from "./components/ModalShowAPIKey";
import NoAPIKey from "./components/NoAPIKey";
import styles from "./index.module.scss";
import { useContainerHeight } from "@/hooks/useContainerHeight";

const RunningAPIMapping = lazy(() => import("./components/RunningAPIMapping"));
const DeployHistory = lazy(
  () => import("../NewAPIMapping/components/DeployHistory")
);

const initPaginationParams = {
  page: 0,
  size: 20,
};

const EnvironmentOverview = () => {
  const [searchParams] = useSearchParams();
  const { currentProduct } = useAppStore();
  const contentRef = useRef<HTMLDivElement>(null)
  const [scrollHeight] = useContainerHeight(contentRef)

  const envId = searchParams.get("envId") || "";

  const { data: envs, isLoading: loadingEnvs } =
    useGetProductEnvs(currentProduct);
  const { data: apiKey, isLoading: loadingApiKey } = useGetAllApiKeyList(
    currentProduct,
    initPaginationParams
  );
  const { data: dataPlane, isLoading: loadingDataPlane, refetch: refetchDataPlane, } =
    useGetAllDataPlaneList(currentProduct, initPaginationParams);
  const { data: runningComponent } = useGetRunningComponentList(currentProduct);
  const { mutateAsync: createApiKeyMutate } = useCreateApiKey();
  const { mutateAsync: rotateApiKeyMutate } = useRotateApiKey();

  const [open, setOpen] = useState(false);
  const [activeTab, setActiveTab] = useState("running_api");
  const [currentEnvId] = useState<string | undefined>();
  const [selectedEnv, setSelectedEnv] = useState<IEnv | undefined>();
  const modalConfirmRef = useRef<any>();

  const rotateApiKey = useCallback(
      async (envId: string, evName: string, closeConfirm = false) => {
        const name = `${evName}_${dayjs.utc().format("YYYY-MM-DD HH:mm:ss")}`;
        try {
          const res = await rotateApiKeyMutate({
            productId: currentProduct,
            envId,
            name,
          } as any);
          closeConfirm && modalConfirmRef?.current?.destroy();
          showModalShowNew(res?.data?.token);
        } catch (e: any) {
          notification.error({ message: e?.data?.error ?? "Rotation failed" });
        }
      },
      [currentProduct, createApiKeyMutate]
  );

  const onConfirmRotate = useCallback(
    (id: string, name: string) => () => rotateApiKey(id, name, true),
    [rotateApiKey]
  );

  const dropdownItems = useCallback(
    (envId: string, envName: string) => [
      {
        key: "refresh-key",
        label: "Rotate API Key",
        onClick: () => {
          modalConfirmRef.current = showModalConfirmRotate(
            envName,
            onConfirmRotate(envId, envName)
          );
        },
      },
    ],
    [onConfirmRotate]
  );

  const getDataPlaneInfo = useCallback(
    (id: string) => {
      const list = dataPlane?.data?.filter((i) => i.envId === id) || [];
      const status = list.every((n) => n.status === "OK");
      const len = list.length;
      const disConnectNum = list.filter((n) => n.status !== "OK").length;
      const connectNum = list.filter((n) => n.status === "OK").length;
      return { len, status, disConnectNum, connectNum };
    },
    [dataPlane]
  );

  useEffect(() => {
    if (!selectedEnv) return;

    const envStatus = getDataPlaneInfo(selectedEnv.id)?.status;

    if (envStatus === false) {
      const interval = setInterval(() => {
        refetchDataPlane();
      }, 5000); 

      return () => clearInterval(interval);
    }
  }, [selectedEnv, dataPlane, refetchDataPlane, getDataPlaneInfo]);


  const envList = useMemo(
    () =>
      sortBy(
        envs?.data.filter(
          (env) => env.name === "production" || env.name === "stage"
        ),
        "name"
      ).reverse(),
    [envs]
  );

  const isHaveApiKey = useMemo(() => {
    if (isEmpty(envs) || every(envs, (i) => isEmpty(i))) return true;
    return (
      selectedEnv &&
      !isEmpty(apiKey?.data?.find((i) => i.envId === selectedEnv.id))
    );
  }, [envs, apiKey, selectedEnv]);

  useEffect(() => {
    if (envList && !envId && isEmpty(selectedEnv?.id)) {
      setSelectedEnv(envList[0]);
    }
    if (envId && envList) {
      setSelectedEnv(envList.find((i) => i.id === envId));
    }
  }, [envList, envId, selectedEnv]);

  return (
    <PageLayout title="Deployment">
      <Flex vertical gap={12} className={styles.pageWrapper}>
        <Flex vertical gap={14} className={styles.scroll}>
          <Spin spinning={loadingEnvs}>
            <div className={styles.overviewContainer}>
              {envList?.map((env) => {
                const haveApiKey = !!apiKey?.data?.find(
                  (i) => i.envId === env.id
                );
                const { disConnectNum, connectNum, len } = getDataPlaneInfo(
                  env.id
                );
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
                    <Flex
                      vertical
                      gap={12}
                      align="start"
                      className={styles.fullWidth}
                    >
                      <Flex
                        align="center"
                        justify="space-between"
                        className={styles.fullWidth}
                      >
                        <Typography.Text
                          ellipsis={{ tooltip: env.name }}
                          style={{
                            marginRight: 16,
                            textTransform: "capitalize",
                            maxWidth: 200,
                            fontSize: 16,
                            fontWeight: 500,
                          }}
                        >
                          {env.name} Environment
                        </Typography.Text>
                        <Dropdown
                          disabled={!haveApiKey}
                          menu={{ items: dropdownItems(env.id, env.name) }}
                        >
                          <MoreOutlined
                            style={{
                              cursor: haveApiKey ? "default" : "not-allowed",
                            }}
                          />
                        </Dropdown>
                      </Flex>
                      <EnvStatus
                        env={env}
                        apiKey={haveApiKey}
                        status={getDataPlaneInfo(env.id)?.status}
                        disConnect={disConnectNum}
                        connect={connectNum}
                        dataPlane={len}
                        loading={loadingDataPlane}
                      />
                    </Flex>
                  </div>
                );
              })}
            </div>
          </Spin>
        </Flex>
        {!isHaveApiKey && !loadingApiKey ? (
          <NoAPIKey env={selectedEnv} />
        ) : (
          <Flex vertical gap={12} className={styles.sectionWrapper}>
            <Flex align="center" justify="space-between">
              <Radio.Group
                onChange={(e) => {
                  setActiveTab(e.target.value);
                }}
                value={activeTab}
              >
                <Radio.Button value="running_api" data-testid="apiMappingTab">
                  Running API mappings
                </Radio.Button>
                <Radio.Button
                  value="deployment_history"
                  data-testid="deploymentHistory"
                >
                  Deployment history
                </Radio.Button>
              </Radio.Group>
            </Flex>

            <main className={styles.pageContent} ref={contentRef}>
              {scrollHeight > 0 && (
                <Suspense fallback={<Spin spinning />}>
                  {activeTab === "running_api" && selectedEnv && (
                    <RunningAPIMapping env={selectedEnv} scrollHeight={scrollHeight ?? 800} />
                  )}
                  {activeTab === "deployment_history" && (
                    <DeployHistory selectedEnv={selectedEnv} scrollHeight={scrollHeight ?? 800} />
                  )}
                </Suspense>
              )}
            </main>
          </Flex>
        )}
        <ModalNewDeployment
          open={open}
          setOpen={setOpen}
          runningComponent={runningComponent}
          currentEnvId={currentEnvId!}
        />
      </Flex>
    </PageLayout>
  );
};

export default EnvironmentOverview;
