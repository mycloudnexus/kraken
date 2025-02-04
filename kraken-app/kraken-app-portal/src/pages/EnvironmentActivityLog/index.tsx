import { PageLayout } from "@/components/Layout";
import LogMethodTag from "@/components/LogMethodTag";
import TrimmedPath from "@/components/TrimmedPath";
import { useGetProductEnvs } from "@/hooks/product";
import { useGetPushButtonEnabled } from "@/hooks/pushApiEvent";
import useSize from "@/hooks/useSize";
import { toDateTime } from "@/libs/dayjs";
import { useAppStore } from "@/stores/app.store";
import { IActivityLog } from "@/utils/types/env.type";
import { Button, Flex, Tabs } from "antd";
import { ColumnsType } from "antd/es/table";
import { startCase } from "lodash";
import { useMemo, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useBoolean } from "usehooks-ts";
import ActivityDetailModal from "./components/ActivityDetailModal";
import EnvironmentActivityTable from "./components/EnvironmentActivityTable";
import PushHistoryList from "./components/PushHistoryList";
import PushHistoryModal from "./components/PushHistoryModal";
import styles from "./index.module.scss";

const EnvironmentActivityLog = () => {
  const { envId } = useParams();
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();
  const { data: envData } = useGetProductEnvs(currentProduct);
  const { data: isPushButtonEnabledResponse } = useGetPushButtonEnabled();
  const ref = useRef<any>();
  const size = useSize(ref);
  const refWrapper = useRef<any>();
  const sizeWrapper = useSize(refWrapper);
  const [mainTabKey, setMainTabKey] = useState<string>("activityLog");
  const { value: isOpen, setTrue: open, setFalse: close } = useBoolean(false);

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

  const columns: ColumnsType<IActivityLog> = [
    {
      key: "name",
      title: "Method",
      render: (log: IActivityLog) => <LogMethodTag method={log.method} />,
      width: 100,
    },
    {
      key: "name",
      title: "Path",
      width: 300,
      render: (log: IActivityLog) => (
        <Flex>
          <TrimmedPath path={log.path} />
        </Flex>
      ),
    },
    {
      key: "buyerName",
      title: "Buyer name",
      width: 200,
      render: (log: IActivityLog) => log.buyerName,
    },
    {
      key: "status",
      title: "Status code",
      width: 140,
      render: (log: IActivityLog) => log.httpStatusCode,
    },
    {
      key: "date",
      title: "Time",
      render: (log: IActivityLog) => toDateTime(log.createdAt),
      width: 200,
    },
    {
      key: "action",
      title: "Action",
      width: 160,
      fixed: "right",
      render: (log: IActivityLog) => (
        <Button
          type="link"
          onClick={() => {
            setModalActivityId(log.requestId);
            setModalOpen(true);
          }}
        >
          View details
        </Button>
      ),
    },
  ];

  const envTabs = useMemo(() => {
    return (
      envData?.data?.map((env) => ({
        key: env.id,
        label: `${startCase(env.name)} Environment`,
        children: (
          <EnvironmentActivityTable
            columns={columns}
            size={size}
            sizeWrapper={sizeWrapper}
          />
        ),
      })) ?? []
    );
  }, [envData]);

  return (
    <PageLayout title="API activity log">
      <Flex align="center" justify="space-between">
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

      <div className={styles.contentWrapper} ref={refWrapper}>
        {isOpen && (
          <PushHistoryModal
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
