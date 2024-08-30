import Text from "@/components/Text";
import { Button, Flex, Input, Table, Tooltip } from "antd";
import styles from "./index.module.scss";
import EnvSelect from "@/components/EnvSelect";
import { useBuyerStore } from "@/stores/buyer.store";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useGetBuyerList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import NewBuyerModal from "./components/NewBuyerModal";
import { useBoolean } from "usehooks-ts";
import { get, isEmpty, omitBy } from "lodash";
import dayjs from "dayjs";
import useDebouncedCallback from "@/hooks/useDebouncedCallback";

const Buyer = () => {
  const { currentProduct } = useAppStore();
  const { params, setParams, resetParams } = useBuyerStore();
  const [search, setSearch] = useState<string | undefined>();
  const { data: dataList, isLoading } = useGetBuyerList(
    currentProduct,
    omitBy(params, isEmpty)
  );
  const {
    value: isModalVisible,
    setFalse: hideModal,
    setTrue: showModal,
  } = useBoolean(false);

  const columns = useMemo(
    () => [
      {
        title: "Company ID",
        dataIndex: "facets",
        render: (r: Record<string, string>) => (
          <Text.LightMedium>{get(r, "buyerInfo.buyerId", "")}</Text.LightMedium>
        ),
        width: 280,
      },
      {
        title: "Company name",
        dataIndex: "facets",
        render: (r: Record<string, string>) => (
          <Text.LightMedium>
            {get(r, "buyerInfo.companyName", "")}
          </Text.LightMedium>
        ),
        width: 280,
      },
      {
        title: "Created at",
        dataIndex: "createdAt",
        render: (text: string) => (
          <Text.LightMedium>
            {dayjs.utc(text).local().format("YYYY-MM-DD HH:mm:ss")}
          </Text.LightMedium>
        ),
      },
      {
        title: "Token Expire At",
        dataIndex: "buyerToken",
        render: (r: Record<string, string>) => (
          <Text.LightMedium>
            {r?.expiredAt
              ? dayjs.utc(r?.expiredAt).local().format("YYYY-MM-DD HH:mm:ss")
              : "-"}
          </Text.LightMedium>
        ),
      },
      {
        title: "Action",
        render: () => (
          <Tooltip title="Not available">
            <Button type="link" disabled>
              Regenerate token
            </Button>
          </Tooltip>
        ),
      },
    ],
    []
  );

  const debouncedFn = useDebouncedCallback(setParams, 500);

  const handleParams = useCallback(
    (e: any) => {
      const { value } = e.target;
      setSearch(value);
      debouncedFn({ buyerId: value });
    },
    [setParams]
  );

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
      <Flex vertical className={styles.paper} gap={16}>
        <Flex justify="space-between" align="center">
          <Text.NormalLarge>
            Buyer list of MEF LSO Sonata Adapters
          </Text.NormalLarge>
          <Flex justify="flex-end" align="center" gap={12}>
            <EnvSelect
              value={params.envId}
              onChange={(e) => setParams({ envId: e })}
            />
            <Input.Search
              placeholder="Search"
              value={search}
              onChange={handleParams}
            />
            <Button type="primary" onClick={showModal}>
              + Add new buyer
            </Button>
          </Flex>
        </Flex>
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
        />
      </Flex>
    </div>
  );
};

export default Buyer;
