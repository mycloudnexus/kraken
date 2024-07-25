import Text from "@/components/Text";
import { LeftOutlined } from "@ant-design/icons";
import { Button, Flex, Input, Table, Tooltip } from "antd";
import { useNavigate } from "react-router-dom";
import styles from "./index.module.scss";
import EnvSelect from "@/components/EnvSelect";
import { useBuyerStore } from "@/stores/buyer.store";
import { useEffect, useMemo } from "react";
import { useGetBuyerList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import NewBuyerModal from "./components/NewBuyerModal";
import { useBoolean } from "usehooks-ts";
import { get } from "lodash";
import dayjs from "dayjs";

const Buyer = () => {
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();
  const { params, setParams, resetParams } = useBuyerStore();
  const { data: dataList, isLoading } = useGetBuyerList(currentProduct, params);
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

  useEffect(() => {
    return () => {
      resetParams();
    };
  }, []);

  return (
    <div className={styles.root}>
      <NewBuyerModal
        open={isModalVisible}
        onClose={hideModal}
        currentEnv={params.envId ?? ""}
      />
      <Flex
        gap={8}
        justify="flex-start"
        style={{ cursor: "pointer", marginBottom: 4 }}
        role="none"
        onClick={() => navigate("/")}
      >
        <LeftOutlined style={{ fontSize: 8 }} />
        <Text.LightLarge color="#434343">
          <span>Home</span>
          <span style={{ color: "#848587" }}>/ Buyer management</span>
        </Text.LightLarge>
      </Flex>
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
              value={params.search}
              onChange={(e) => setParams({ search: e.target.value })}
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
