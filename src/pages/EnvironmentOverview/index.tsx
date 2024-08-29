import { useState, useCallback, useMemo, useEffect, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import dayjs from "dayjs";
import clsx from "clsx";
import { Dropdown, Flex, Radio, Spin, Typography, notification } from "antd";
import { MoreOutlined } from "@ant-design/icons";
import { every, isEmpty, sortBy } from "lodash";
import {
  useGetProductEnvs,
  useGetAllApiKeyList,
  useGetAllDataPlaneList,
  useGetRunningComponentList,
  useCreateApiKey,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { ROUTES } from "@/utils/constants/route";
import EnvStatus from "./components/EnvStatus";
import { showModalConfirmRotate } from "./components/ModalConfirmRotateAPIKey";
import ModalNewDeployment from "./components/ModalNewDeployment";
import { showModalShowNew } from "./components/ModalShowAPIKey";
import RunningAPIMapping from "./components/RunningAPIMapping";
import NoAPIKey from "./components/NoAPIKey";
import styles from "./index.module.scss";
import { IEnv } from "@/utils/types/env.type";
import DeployHistory from "../NewAPIMapping/components/DeployHistory";

const initPaginationParams = {
  page: 0,
  size: 20,
};

const EnvironmentOverview = () => {
  const observedDiv = useRef<HTMLDivElement>(null);
  const [height, setHeight] = useState<number | undefined>(undefined);

  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { currentProduct } = useAppStore();

  const envId = searchParams.get("envId") || "";

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
  const [activeTab, setActiveTab] = useState("running_api");
  const [currentEnvId] = useState<string | undefined>();
  const [selectedEnv, setSelectedEnv] = useState<IEnv | undefined>();
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
        notification.error({ message: e?.data?.error || "Generate failed" });
      }
    },
    [currentProduct, createApiKeyMutate]
  );

  const onConfirmRotate = useCallback(
    (id: string, name: string) => () => generateApiKey(id, name, true),
    [generateApiKey]
  );

  const dropdownItems = useCallback(
    (envId: string, envName: string) => [
      {
        key: "view-log",
        label: "API Activity Log",
        onClick: () => navigate(ROUTES.ENV_ACTIVITY_LOG(envId)),
      },
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
    [navigate, onConfirmRotate]
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

  useEffect(() => {
    if (!observedDiv?.current) {
      return;
    }
    const resizeObserver = new ResizeObserver(() => {
      if (observedDiv.current?.offsetHeight !== height) {
        setHeight(observedDiv?.current?.offsetHeight ?? 0);
      }
    });
    resizeObserver.observe(observedDiv.current);

    return function cleanup() {
      resizeObserver.disconnect();
    };
  }, [observedDiv.current]);

  return (
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
                    />
                  </Flex>
                </div>
              );
            })}
          </div>
        </Spin>
      </Flex>
      {!isHaveApiKey ? (
        <NoAPIKey env={selectedEnv} />
      ) : (
        <Flex
          vertical
          gap={12}
          className={styles.sectionWrapper}
          ref={observedDiv}
        >
          <Flex align="center" justify="space-between">
            <Radio.Group
              onChange={(e) => {
                setActiveTab(e.target.value);
              }}
              value={activeTab}
              style={{
                marginBottom: 8,
              }}
            >
              <Radio.Button value="running_api">
                Running API mappings
              </Radio.Button>
              <Radio.Button value="deployment_history">
                Deployment history
              </Radio.Button>
            </Radio.Group>
          </Flex>
          {activeTab === "running_api" && (
            <RunningAPIMapping scrollHeight={height} env={selectedEnv} />
          )}
          {activeTab === "deployment_history" && (
            <DeployHistory scrollHeight={height} selectedEnv={selectedEnv} />
          )}
        </Flex>
      )}
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
