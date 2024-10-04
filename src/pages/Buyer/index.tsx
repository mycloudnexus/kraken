import Text from "@/components/Text";
import { Button, Flex, Popconfirm, Table, notification } from "antd";
import styles from "./index.module.scss";
import { useBuyerStore } from "@/stores/buyer.store";
import { useEffect, useMemo, useRef } from "react";
import {
  useActiveBuyer,
  useDeactiveBuyer,
  useGetBuyerList,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import NewBuyerModal from "./components/NewBuyerModal";
import { useBoolean } from "usehooks-ts";
import { get, isEmpty, omitBy } from "lodash";
import useDebouncedCallback from "@/hooks/useDebouncedCallback";
import EnvTabs from "@/components/EnvTabs";
import { IBuyer } from "@/utils/types/component.type";
import { ContentTime } from "../NewAPIMapping/components/DeployHistory";
import BuyerStatus from "@/components/BuyerStatus";
import RegenToken from "./components/RegenToken";
import useSize from "@/hooks/useSize";

const Buyer = () => {
  const { currentProduct } = useAppStore();
  const { params, setParams, resetParams } = useBuyerStore();
  const { data: dataList, isLoading } = useGetBuyerList(
    currentProduct,
    omitBy(params, isEmpty)
  );
  const {
    value: isModalVisible,
    setFalse: hideModal,
    setTrue: showModal,
  } = useBoolean(false);
  const { mutateAsync: runActive } = useActiveBuyer();
  const { mutateAsync: runDeactive } = useDeactiveBuyer();
  const ref = useRef<any>();
  const size = useSize(ref);
  console.log("🚀 ~ Buyer ~ size:", size);

  const handleActive = async (id: string) => {
    try {
      const res = await runActive({
        productId: currentProduct,
        id,
      } as any);
      if (res) {
        notification.success({
          message: get(res, "message", "Success!"),
        });
      }
    } catch (error) {
      notification.error({
        message: get(error, "reason", "Error. Please try again"),
      });
    }
  };

  const handleDeactive = async (id: string) => {
    try {
      const res = await runDeactive({
        productId: currentProduct,
        id,
      } as any);
      if (res) {
        notification.success({
          message: get(res, "message", "Success!"),
        });
      }
    } catch (error) {
      notification.error({
        message: get(error, "reason", "Error. Please try again"),
      });
    }
  };

  const columns = useMemo(
    () => [
      {
        title: "Company ID",
        dataIndex: "facets",
        render: (r: Record<string, string>) => (
          <Text.LightMedium>{get(r, "buyerInfo.buyerId", "")}</Text.LightMedium>
        ),
        width: 180,
      },
      {
        title: "Company name",
        dataIndex: "facets",
        render: (r: Record<string, string>) => (
          <Text.LightMedium>
            {get(r, "buyerInfo.companyName", "")}
          </Text.LightMedium>
        ),
      },
      {
        title: "Buyer status",
        dataIndex: "metadata",
        width: 180,
        render: (r: Record<string, string>) => (
          <BuyerStatus status={r.status} />
        ),
      },
      {
        title: "Created by",
        dataIndex: "",
        width: 180,
        render: (record: IBuyer) => (
          <ContentTime content={record.createdBy} time={record.createdAt} />
        ),
      },
      {
        title: "Action",
        width: 300,
        render: (record: IBuyer) => (
          <Flex align="center" gap={18}>
            {record?.metadata?.status === "activated" && (
              <Popconfirm
                overlayClassName={styles.popconfirm}
                title={undefined}
                description={
                  <>
                    Are you sure to deactivate this buyer?
                    <br />
                    Token will be expired immediately after this action.
                  </>
                }
                onConfirm={() => handleDeactive(record.id)}
                okText="Deactivate"
                cancelText="Cancel"
                okButtonProps={{
                  danger: true,
                }}
              >
                <Button className={styles.btn} type="link">
                  Deactivate buyer
                </Button>
              </Popconfirm>
            )}
            {record?.metadata?.status === "deactivated" && (
              <Button
                className={styles.btn}
                type="link"
                onClick={() => handleActive(record.id)}
              >
                Activate buyer
              </Button>
            )}
            <RegenToken buyer={record} />
          </Flex>
        ),
      },
    ],
    [handleActive, handleDeactive]
  );

  const debouncedFn = useDebouncedCallback(setParams, 500);

  useEffect(() => {
    return () => {
      resetParams();
      debouncedFn.cancel();
    };
  }, [resetParams, debouncedFn]);

  return (
    <div className={styles.root}>
      <NewBuyerModal
        open={isModalVisible}
        onClose={hideModal}
        currentEnv={params.envId ?? ""}
      />
      <Text.LightLarge>Buyer management</Text.LightLarge>
      <Flex vertical className={styles.paper} gap={16}>
        <Flex justify="space-between" align="center">
          <EnvTabs
            value={params.envId}
            onChange={(e) => setParams({ envId: e })}
          />
          <Flex justify="flex-end" align="center" gap={12}>
            <Button type="primary" onClick={showModal}>
              + Add new buyer
            </Button>
          </Flex>
        </Flex>
        <div style={{ flex: 1, overflowY: "auto" }} ref={ref}>
          <Table
            loading={isLoading}
            pagination={
              dataList?.total < 50
                ? false
                : {
                    current: dataList?.page + 1,
                    total: dataList?.total,
                    size: dataList?.size,
                    onChange: (page, pageSize) => {
                      setParams({ page: page - 1, size: pageSize });
                    },
                  }
            }
            dataSource={dataList?.data}
            columns={columns}
            scroll={{ y: get(size, "height", 0) - 80, x: "auto" }}
          />
        </div>
      </Flex>
    </div>
  );
};

export default Buyer;
