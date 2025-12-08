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

export type UserValue = {
    value: string,
    label: string,
}
export type BuyerPageData = {
    data: {
        data: unknown;
    },
    total: number,
    page: number,
    size: number,
}

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
  const [page, setPage] = useState(0);

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
  const [value, setValue] = useState<UserValue | undefined | null>();
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
            page={page}
          />
        ),
      })) ?? []
    );
  }, [envData, pathQuery, buyerQuery, page]);

  const searchPathQuery = (value: string) => {
    setPathQuery(value);
    setPage(0);
  };
  const fetchBuyerList = (buyer: string): Promise<UserValue[] | void> => {
        console.log(value)
        if (buyer) {
            const response: Promise<BuyerPageData> = getBuyerList(currentProduct, {page: 0, size: 30, buyerId: buyer, envId});
            return response.then((res) => res?.data?.data).then((res) => {
                const results = Array.isArray(res) ? res : [];
                return results.map((item) => ({
                    value: item.facets.buyerInfo.buyerId,
                    label: item.facets.buyerInfo.companyName,
                } as UserValue));
            }).then((newOptions) => setOptions(newOptions));
        } else {
            setOptions([]);
            return Promise.resolve();
        }
    }

  const handleChange = (buyer: UserValue) => {
      setValue(buyer);
      setBuyerQuery(buyer?.value ?? '');
      setPage(0);
      setOptions([]);
  }
  return (
    <PageLayout
      title={
        <Flex
          style={
                { width: "100%" }
            }
          justify="space-between"
          vertical={false}
          align="center"
        >
          <Tabs
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
            onChange={setMainTabKey}
            activeKey={mainTabKey}
            hideAdd
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
              items={envTabs}
              onChange={
                (key) => {
                  navigate(`/env/${key}`);
                  setOptions([]);
                  setValue(null);
                  setBuyerQuery('')
                }
              }
              activeKey={envId}
              type="card"
              tabBarExtraContent={
                  <div>
                      <Search
                          placeholder="Please copy full path here"
                          style={
                            { width: "250px",
                              marginRight: "20px"
                            }
                          }
                          onSearch={searchPathQuery}
                          allowClear
                      />
                      <Select
                          id = "select-buyer"
                          title="select-buyer"
                          disabled={false}
                          labelInValue
                          value={value}
                          filterOption={false}
                          style={
                            { width: "250px"
                            }
                          }
                          showSearch
                          onSearch={
                            fetchBuyerList
                          }
                          onChange={handleChange}
                          placeholder="Please select buyer"
                          notFoundContent={
                           'No results found'
                          }
                          options={
                            options
                          }
                          autoClearSearchValue
                          allowClear
                      />
                  </div>
              }
            />
          ) : (
            <PushHistoryList />
            //test1
          )}
        </div>
      </div>

      <ActivityDetailModal
        open={modalOpen}
        activityId={modalActivityId ?? ""}
        envId={String(envId)}
        setOpen={(value) => setModalOpen(value)}
      />
    </PageLayout>
  );
};

export default EnvironmentActivityLog;
