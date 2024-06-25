import ServerIcon from "@/assets/server-icon.svg";
import Flex from "@/components/Flex";
import LogMethodTag from "@/components/LogMethodTag";
import Text from "@/components/Text";
import { useGetComponentList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { COMPONENT_KIND_API_TARGET_SPEC } from "@/utils/constants/product";
import { IComponent } from "@/utils/types/product.type";
import { DownOutlined, RightOutlined } from "@ant-design/icons";
import { Button, Empty, Spin, Tooltip, Typography } from "antd";
import clsx from "clsx";
import jsYaml from "js-yaml";
import { get, isEmpty } from "lodash";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import swaggerClient from "swagger-client";
import { useBoolean } from "usehooks-ts";
import styles from "./index.module.scss";

type ItemProps = {
  item: IComponent;
};

const APIItem = ({ item }: ItemProps) => {
  const navigate = useNavigate();
  const { currentProduct } = useAppStore();
  const { sellerApi, setSellerApi, setServerKey } = useNewApiMappingStore();
  const { value: isOpen, toggle: toggleOpen } = useBoolean(true);
  const baseSpec = useMemo(() => {
    const encoded = item?.facets?.baseSpec?.content;
    if (!encoded) return undefined;
    const yamlContent = atob(encoded.slice(31))
      .replace(/(â)/g, "")
      .replace(/(â)/g, "");
    return jsYaml.load(yamlContent);
  }, [item]);

  const [resolvedSpec, setResolvedSpec] = useState<any>();
  useEffect(() => {
    if (!baseSpec) return;
    (async () => {
      const result = await swaggerClient.resolve({ spec: baseSpec });
      setResolvedSpec(result.spec);
    })();
  }, [baseSpec]);

  const onSelect = (key: string) => {
    const name = get(item, "metadata.name");
    const serverKey = get(item, "metadata.key");
    const [url, method] = key.split(" ");
    if (!resolvedSpec) {
      const selectedSellerApi = {
        name,
        url,
        method,
        spec: undefined,
      };
      setSellerApi(selectedSellerApi);
      setServerKey(serverKey);
    }
    const listSpec: any[] = [];
    Object.entries(resolvedSpec.paths).forEach(
      ([path, methodObj]: [string, any]) => {
        Object.entries(methodObj).forEach(([method, spec]) => {
          listSpec.push({
            name,
            url: path,
            method,
            spec,
          });
        });
      }
    );
    const selectedSpec = listSpec.find(
      (item) => item.url === url && item.method === method
    );
    setSellerApi(selectedSpec);
    setServerKey(serverKey);
  };
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
            const active =
              item?.metadata?.name === sellerApi?.name &&
              url === sellerApi?.url &&
              method === sellerApi?.method;
            return (
              <div
                className={clsx(styles.card, {
                  [styles.active]: active,
                })}
                onClick={() => onSelect(key)}
                role="none"
              >
                <Flex justifyContent="flex-start" gap={8} alignItems="center">
                  <div style={{ flex: "0 0 16px", width: "16px" }}>
                    <ServerIcon />
                  </div>
                  <Typography.Text ellipsis={{ tooltip: true }}>
                    {url?.replace("/", "")}
                  </Typography.Text>
                </Flex>
                <Flex
                  justifyContent="flex-start"
                  gap={12}
                  style={{ marginTop: 12 }}
                >
                  <LogMethodTag method={method} />
                  <Tooltip title={url}>
                    <Typography.Text ellipsis={{ tooltip: true }}>
                      {url}
                    </Typography.Text>
                  </Tooltip>
                </Flex>
              </div>
            );
          })}
      </Flex>
    </div>
  );
};

const SelectAPI = () => {
  const { currentProduct } = useAppStore();
  const { data: dataList, isLoading } = useGetComponentList(currentProduct, {
    kind: COMPONENT_KIND_API_TARGET_SPEC,
    size: 1000,
  });
  return (
    <Spin spinning={isLoading} className={styles.loading}>
      <Text.BoldLarge>Select API</Text.BoldLarge>
      <div className={styles.content}>
        {dataList?.data?.map((item: IComponent) => (
          <APIItem item={item} key={item.id} />
        ))}
        {isEmpty(dataList?.data) && <Empty />}
      </div>
    </Spin>
  );
};

export default SelectAPI;
