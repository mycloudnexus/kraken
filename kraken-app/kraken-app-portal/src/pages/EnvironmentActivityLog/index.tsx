import { PageLayout } from "@/components/Layout";
import { useGetProductEnvs } from "@/hooks/product";
import { useGetPushButtonEnabled } from "@/hooks/pushApiEvent";
import { useAppStore } from "@/stores/app.store";
import { Button, Flex, Tabs, Input } from "antd";
import { startCase } from "lodash";
import { useMemo, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useBoolean } from "usehooks-ts";
import ActivityDetailModal from "./components/ActivityDetailModal";
import EnvironmentActivityTable from "./components/EnvironmentActivityTable";
import PushHistoryDrawer from "./components/PushHistoryDrawer";
import PushHistoryList from "./components/PushHistoryList";
import styles from "./index.module.scss";

const { Search } = Input;

const EnvironmentActivityLog = () => {
  const { envId } = useParams();
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();
  const { data: envData } = useGetProductEnvs(currentProduct);
  const { data: isPushButtonEnabledResponse } = useGetPushButtonEnabled();
  const refWrapper = useRef<any>();
  const [mainTabKey, setMainTabKey] = useState<string>("activityLog");
  const { value: isOpen, setTrue: open, setFalse: close } = useBoolean(false);
  const [pathQuery, setPathQuery] = useState("");

  const envOptions = useMemo(() => {
    return (
      envData?.data?.map((env) => ({
        value: env.id,
        label: env.name,
      })) ?? []
    );
  }, [envData]);

  const [modalActivityId, setModalActivityId] = useState<string | undefined>();
  const [modalOpen, setModalOpen] = useState(false);
  const isActivityLogActive = useMemo(
    () => mainTabKey === "activityLog",
    [mainTabKey]
  );

  const openActionModal = (requestId: string) => {
    setModalActivityId(requestId);
    setModalOpen(true);
  };

  const envTabs = useMemo(() => {
    return (
      envData?.data?.map((env) => ({
        key: env.id,
        label: `${startCase(env.name)} Environment`,
        children: (
          <EnvironmentActivityTable
            openActionModal={openActionModal}
            pathQuery={pathQuery}
          />
        ),
      })) ?? []
    );
  }, [envData, pathQuery]);

  const searchPathQuery = (value: string) => {
    setPathQuery(value);
  };

  return (
    <PageLayout
      title={
        <Flex
          align="center"
          justify="space-between"
          vertical={false}
          style={{ width: "100%" }}
        >
          <Tabs
            activeKey={mainTabKey}
            hideAdd
            onChange={setMainTabKey}
            items={[
              {
                label: "Activity log",
                key: "activityLog",
              },
              {
                label: "Push history",
                key: "pushHistory",
              },
            ]}
          />
          {isActivityLogActive && !!isPushButtonEnabledResponse?.enabled && (
            <Button type="primary" onClick={open}>
              Push log
            </Button>
          )}
        </Flex>
      }
    >
      <div className={styles.contentWrapper} ref={refWrapper}>
        {isOpen && (
          <PushHistoryDrawer
            isOpen={isOpen}
            envOptions={envOptions}
            onClose={close}
          />
        )}

        <div className={styles.tableWrapper}>
          {isActivityLogActive ? (
            <Tabs
              type="card"
              activeKey={envId}
              items={envTabs}
              onChange={(key) => {
                navigate(`/env/${key}`);
              }}
              tabBarExtraContent={
                <Search
                  placeholder="Please copy full path here"
                  style={{ width: "250px" }}
                  onSearch={searchPathQuery}
                  allowClear
                />
              }
            />
          ) : (
            <PushHistoryList />
          )}
        </div>
      </div>

      <ActivityDetailModal
        envId={String(envId)}
        activityId={modalActivityId ?? ""}
        open={modalOpen}
        setOpen={(value) => setModalOpen(value)}
      />
    </PageLayout>
  );
};

export default EnvironmentActivityLog;
