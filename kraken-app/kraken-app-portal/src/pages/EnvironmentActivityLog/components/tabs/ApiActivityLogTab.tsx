import { useGetProductEnvs } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { Button, Tabs, Input } from "antd";
import { startCase } from "lodash";
import { useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useBoolean } from "usehooks-ts";
import EnvironmentActivityTable from "../EnvironmentActivityTable";
import PushHistoryDrawer from "../PushHistoryDrawer";
import ActivityDetailModal from "../ActivityDetailModal";

const { Search } = Input;

const ActivityLogTab = () => {
  const { envId } = useParams();
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();
  const { data: envData } = useGetProductEnvs(currentProduct);
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
    <>
      <Tabs
        type="card"
        activeKey={envId}
        items={envTabs}
        onChange={(key) => {
          navigate(`/env/${key}`);
        }}
        tabBarExtraContent={
          <div className="tab">
            <Search
                placeholder="Please copy full path here"
                style={{ width: "250px" }}
                onSearch={searchPathQuery}
                allowClear />
            <Button type="primary" onClick={open}>
              Push log
            </Button>
          </div>
        }
      />
      {isOpen && (
        <PushHistoryDrawer
          isOpen={isOpen}
          envOptions={envOptions}
          onClose={close}
        />
      )}

      <ActivityDetailModal
        envId={String(envId)}
        activityId={modalActivityId ?? ""}
        open={modalOpen}
        setOpen={(value) => setModalOpen(value)}
      />
      </>
  );
};

export default ActivityLogTab;
