import BuyerStatus from "@/components/BuyerStatus";
import EnvTabs from "@/components/EnvTabs";
import { PageLayout } from "@/components/Layout";
import { Text } from "@/components/Text";
import {
  useActiveBuyer,
  useDeactiveBuyer,
  useGetBuyerList, useRetrieveToken,
} from "@/hooks/product";
import useDebouncedCallback from "@/hooks/useDebouncedCallback";
import useSize from "@/hooks/useSize";
import { useAppStore } from "@/stores/app.store";
import { useBuyerStore } from "@/stores/buyer.store";
import {IBuyer, IBuyerToken} from "@/utils/types/component.type";
import { Button, Flex, Popconfirm, Table, notification } from "antd";
import { get } from "lodash";
import { useEffect, useMemo, useRef, useState } from "react";
import { useBoolean } from "usehooks-ts";
import { ContentTime } from "../NewAPIMapping/components/DeployHistory/ContentTime";
import NewBuyerModal from "./components/NewBuyerModal";
import RegenToken from "./components/RegenToken";
import TokenModal from "./components/TokenModal";
import styles from "./index.module.scss";
import {useUser} from "@/hooks/user/useUser.tsx";
import {ERole} from "@/components/Role";

const Buyer = () => {
  const { currentProduct } = useAppStore();
  const { params, setParams, resetParams } = useBuyerStore();
  const { data: dataList, isLoading } = useGetBuyerList(currentProduct, params);
  const { mutateAsync: retrieveToken } = useRetrieveToken();
  const {
    value: isModalVisible,
    setFalse: hideModal,
    setTrue: showModal,
  } = useBoolean(false);

  const {
    value: isReactivateModalVisible,
    setFalse: hideReactivateModal,
    setTrue: showReactivateModal,
  } = useBoolean(false);
  const [responseItem, setResponseItem] = useState<IBuyerToken>();
  const [tokenItem, setTokenItem] = useState<IBuyerToken>();

  const { mutateAsync: runActive } = useActiveBuyer();
  const { mutateAsync: runDeactive } = useDeactiveBuyer();
  const ref = useRef<any>();
  const size = useSize(ref);
  const { currentUser } = useUser();
  const isAdmin = useMemo(
      () => currentUser?.role === ERole.ADMIN,
      [currentUser?.role]
  );


  const handleActive = async (id: string) => {
    try {
      const res = await runActive({
        productId: currentProduct,
        id,
      } as any);
      if (res) {
        setResponseItem(get(res, "data"));
        showReactivateModal();
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


  const RetrieveToken = async (id: string) => {
    const params = {buyerId: id, productId: currentProduct} as any;
    const res = await retrieveToken(params);
    setTokenItem({
      buyerToken: get(res, "data")
    });
    showReactivateModal();
  }

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
        width: 150,
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
        title: "Buyer API token",
        dataIndex: "",
        width: 200,
        hidden: !isAdmin,
        render: (record: IBuyer) => (
                  <div>
                    <span style={
                      { width: '200px',
                        filter: 'blur(5px)',
                        position: "relative"
                      }
                    }
                    >
                      xxxxxxxxxxxxxxxxxxxxxxxxxxx
                    </span>
                    <span style={
                      { position: 'absolute',
                        left: '30px'}
                      }
                    >
                      <button
                          style={
                            { color: 'red',
                              background: 'white',
                              borderColor: '#ffa39e'
                            }
                          }
                          onClick={() =>
                              RetrieveToken(record.id)}
                      >
                        Reveal Buyer Token
                      </button>
                    </span>
                  </div>
                )
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
                <Button
                  className={styles.btn}
                  type="link"
                  data-testid={`${record.id}-deactivate`}
                >
                  Deactivate buyer
                </Button>
              </Popconfirm>
            )}
            {record?.metadata?.status === "deactivated" && (
              <Button
                className={styles.btn}
                type="link"
                onClick={() => handleActive(record.id)}
                data-testid={`${record.id}-activate`}
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
    <PageLayout title="Buyer management">
      <Flex vertical className={styles.paper} gap={12}>
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
            pagination={{
              current: dataList?.page + 1,
              total: dataList?.total,
              size: dataList?.size,
              showSizeChanger: true,
              onChange: (page, pageSize) => {
                setParams({ page: page - 1, size: pageSize });
              },
            }}
            dataSource={dataList?.data}
            columns={columns}
            scroll={{ y: get(size, "height", 0) - 60, x: "auto" }}
          />
        </div>
      </Flex>

      <NewBuyerModal
        open={isModalVisible}
        onClose={hideModal}
        currentEnv={params.envId ?? ""}
      />

      {responseItem && (
        <TokenModal
          open={isReactivateModalVisible}
          onClose={hideReactivateModal}
          item={responseItem}
        />
      )}
      {tokenItem && (
          <TokenModal
              item={tokenItem}
              open={isReactivateModalVisible}
              onClose={hideReactivateModal}
          />
      )}
    </PageLayout>
  );
};

export default Buyer;
