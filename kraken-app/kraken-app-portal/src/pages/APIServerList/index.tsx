import Flex from "@/components/Flex";
import { PageLayout } from "@/components/Layout";
import { Text } from "@/components/Text";
import { useGetSellerAPIList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IComponent } from "@/utils/types/component.type";
import { Button, Empty, Spin } from "antd";
import { isEmpty } from "lodash";
import { useNavigate } from "react-router";
import APIServerCard from "./components/APIServerCard";
import styles from "./index.module.scss";

const APIServerList = () => {
  const { currentProduct } = useAppStore();
  const {
    data: dataList,
    isLoading,
    isRefetching,
    refetch,
  } = useGetSellerAPIList(currentProduct);
  const navigate = useNavigate();

  return (
    <PageLayout
      title={
        <>
          <Text.LightLarge>Seller API Setup</Text.LightLarge>
          <Button
            type="primary"
            onClick={() => navigate(`/component/${currentProduct}/new`)}
          >
            + Create API server
          </Button>
        </>
      }
    >
      <div className={styles.content}>
        <Spin spinning={isLoading || isRefetching}>
          <Flex
            flexDirection="column"
            alignItems="flex-start"
            gap={26}
            style={{ width: "100%" }}
          >
            {dataList?.data?.map((item: IComponent) => (
              <div key={item.id} style={{ width: "100%" }}>
                <APIServerCard item={item} refetchList={refetch} />
              </div>
            ))}
          </Flex>
          {isEmpty(dataList?.data) && (
            <Flex style={{ height: "50vh" }}>
              <Empty />
            </Flex>
          )}
        </Spin>
      </div>
    </PageLayout>
  );
};

export default APIServerList;
