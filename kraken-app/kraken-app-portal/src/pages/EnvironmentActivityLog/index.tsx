import { PageLayout } from "@/components/Layout";
import {useGetProductEnvs} from "@/hooks/product";
import { useGetPushButtonEnabled } from "@/hooks/pushApiEvent";
import { useAppStore } from "@/stores/app.store";
import {Button, Flex, Tabs, Input, Select} from "antd";
import { startCase } from "lodash";
import { useMemo, useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useBoolean } from "usehooks-ts";
import ActivityDetailModal from "./components/ActivityDetailModal";
import EnvironmentActivityTable from "./components/EnvironmentActivityTable";
import PushHistoryDrawer from "./components/PushHistoryDrawer";
import PushHistoryList from "./components/PushHistoryList";
import styles from "./index.module.scss";
import {getBuyerList} from "@/services/products.ts";

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
  const [buyerQuery, setBuyerQuery] = useState("");

  const envOptions = useMemo(() => {
    return (
      envData?.data?.map((env) => ({
        value: env.id,
        label: env.name,
      })) ?? []
    );
  }, [envData]);

  const [modalActivityId, setModalActivityId] = useState<string | undefined>();
  const [options, setOptions] = useState<UserValue[]>([]);
  const [value, setValue] = useState<UserValue>();
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
            buyerQuery={buyerQuery}
          />
        ),
      })) ?? []
    );
  }, [envData, pathQuery, buyerQuery]);

  const searchPathQuery = (value: string) => {
    setPathQuery(value);
  };
    const fetchBuyerList = (buyer: string): Promise<UserValue[] | void> => {
        console.log(value)
        const response: Promise<{data: {data: unknown}}> = getBuyerList(currentProduct, {page: 0, size: 30, buyerId: buyer});
        return response.then((res) => res?.data?.data).then((res) => {
            const results = Array.isArray(res) ? res : [];
            return results.map((item) => ({
                value: item.facets.buyerInfo.buyerId,
                label: item.facets.buyerInfo.companyName,
            } as UserValue));
        }).then((newOptions) => setOptions(newOptions));
    }

  const handleChange = (buyer: UserValue) => {
      setValue(buyer);
      setBuyerQuery(buyer?.value ?? '');
  }
    interface UserValue {
        value: string,
        label: string,
    }
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
                  <div>
                      <Search
                          placeholder="Please copy full path here"
                          style={{ width: "250px", marginRight: "20px"}}
                          onSearch={searchPathQuery}
                          allowClear
                      />
                      <Select
                          title="select-buyer"
                          labelInValue
                          filterOption={false}
                          style={{ width: "250px"}}
                          showSearch
                          onSearch={fetchBuyerList}
                          onChange={handleChange}
                          placeholder="Please select buyer"
                          notFoundContent={'No results found'}
                          options={options}
                          autoClearSearchValue
                          allowClear
                      />
                  </div>
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
