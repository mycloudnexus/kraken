import Text from "@/components/Text";
import { useGetComponentList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { API_SERVER_KEY } from "@/utils/constants/product";
import styles from "./index.module.scss";
import { IComponent } from "@/utils/types/product.type";
import { useBoolean } from "usehooks-ts";
import Flex from "@/components/Flex";
import { DownOutlined, RightOutlined } from "@ant-design/icons";
import { get, isEmpty } from "lodash";
import { Button, Empty, Spin, Tooltip } from "antd";
import { useNavigate } from "react-router-dom";
import RequestMethod from "@/components/Method";
import ServerIcon from "@/assets/server-icon.svg";

type Props = {
  onSelect?: (value: any) => void;
};

type ItemProps = {
  item: IComponent;
  onSelect?: (value: any) => void;
};

const APIItem = ({ item, onSelect }: ItemProps) => {
  const navigate = useNavigate();
  const { currentProduct } = useAppStore();
  const { value: isOpen, toggle: toggleOpen } = useBoolean(true);
  return (
    <div>
      <Flex justifyContent="space-between">
        <Flex justifyContent="flex-start" gap={8} alignItems="center">
          {isOpen ? (
            <DownOutlined onClick={toggleOpen} style={{ fontSize: 10 }} />
          ) : (
            <RightOutlined onClick={toggleOpen} style={{ fontSize: 10 }} />
          )}
          <Text.LightMedium>{get(item, "metadata.name")}</Text.LightMedium>
        </Flex>
        <Button
          type="text"
          style={{ color: "#2962FF", padding: 0 }}
          onClick={() =>
            navigate(
              `/component/${currentProduct}/edit/${get(
                item,
                "metadata.key"
              )}/api`
            )
          }
        >
          Add API
        </Button>
      </Flex>
      <Flex
        flexDirection="column"
        gap={12}
        alignItems="flex-start"
        justifyContent="flex-start"
      >
        {isOpen &&
          item?.facets?.selectedAPIs?.map((key: string) => {
            const [url, method] = key.split(" ");
            return (
              <div
                className={styles.card}
                onClick={() => onSelect?.(key)}
                role="none"
              >
                <Flex justifyContent="flex-start" gap={8} alignItems="center">
                  <ServerIcon />
                  <Text.LightMedium>{url?.replace("/", "")}</Text.LightMedium>
                </Flex>
                <Flex
                  justifyContent="flex-start"
                  gap={12}
                  style={{ marginTop: 12 }}
                >
                  <RequestMethod method={method} />
                  <Tooltip title={url}>
                    <Text.LightMedium>{url}</Text.LightMedium>
                  </Tooltip>
                </Flex>
              </div>
            );
          })}
      </Flex>
    </div>
  );
};

const SelectAPI = ({ onSelect }: Props) => {
  const { currentProduct } = useAppStore();
  const { data: dataList, isLoading } = useGetComponentList(currentProduct, {
    kind: API_SERVER_KEY,
    size: 1000,
  });
  return (
    <Spin spinning={isLoading} className={styles.loading}>
      <Text.BoldLarge>Select API</Text.BoldLarge>
      <div className={styles.content}>
        {dataList?.data?.map((item: IComponent) => (
          <APIItem item={item} onSelect={onSelect} key={item.id} />
        ))}
        {isEmpty(dataList?.data) && <Empty />}
      </div>
    </Spin>
  );
};

export default SelectAPI;
