import Flex from "@/components/Flex";
import { Text } from "@/components/Text";
import { useGetSellerAPIList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IComponent } from "@/utils/types/component.type";
import { Button, ConfigProvider, Empty, Spin } from "antd";
import { isEmpty } from "lodash";
import { useNavigate } from "react-router";
import APIServerCard from "./components/APIServerCard";
import styles from "./index.module.scss";

const APIServerList = () => {
  const { currentProduct } = useAppStore();
  const {
    data: dataList,
    isLoading,
    refetch,
  } = useGetSellerAPIList(currentProduct);
  const navigate = useNavigate();

  return (
    <ConfigProvider table={{ style: { borderColor: "#F0F0F0" } }}>
      <div className={styles.root}>
        <Flex justifyContent="space-between">
          <Text.LightLarge>Seller API Setup</Text.LightLarge>
          <Button
            type="primary"
            onClick={() => navigate(`/component/${currentProduct}/new`)}
          >
            + Create API server
          </Button>
        </Flex>
        <div className={styles.content}>
          <Spin spinning={isLoading}>
            <Flex
              flexDirection="column"
              alignItems="flex-start"
              gap={26}
              style={{ width: "100%" }}
            >
              {dataList?.data?.map((item: IComponent) => (
                <APIServerCard
                  item={item}
                  key={item.id}
                  refetchList={refetch}
                />
              ))}
            </Flex>
            {isEmpty(dataList?.data) && (
              <Flex style={{ height: "50vh" }}>
                <Empty />
              </Flex>
            )}
          </Spin>
        </div>
      </div>
    </ConfigProvider>
  );
};

export default APIServerList;
