import Flex from "@/components/Flex";
import Text from "@/components/Text";
import { useGetComponentListAPISpec } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { IComponent } from "@/utils/types/product.type";
import { DownOutlined, RightOutlined } from "@ant-design/icons";
import { Button, Input, Spin, Tooltip, Typography, notification } from "antd";
import clsx from "clsx";
import jsYaml from "js-yaml";
import { cloneDeep, delay, get, isEmpty } from "lodash";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import swaggerClient from "swagger-client";
import { useBoolean } from "usehooks-ts";
import styles from "./index.module.scss";
import useGetApiSpec from "../useGetApiSpec";
import EmptyIcon from "@/assets/newAPIServer/empty.svg";
import RequestMethod from "@/components/Method";
import { extractOpenApiStrings } from "@/utils/helpers/schema";
import { decode } from "js-base64";

type ItemProps = {
  item: IComponent;
  isOneItem: boolean;
  setSellerApi: (api: any) => void;
  selectedAPI: any;
  setSelectedServer: (server: string) => void;
};

export const APIItem = ({
  item,
  isOneItem,
  setSellerApi,
  selectedAPI,
  setSelectedServer,
}: ItemProps) => {
  const { sellerApi } = useNewApiMappingStore();
  const { value: isOpen, toggle: toggleOpen, setTrue } = useBoolean(false);
  const [searchValue, setSearchValue] = useState("");
  const { value: firstOpen, setFalse } = useBoolean(true);

  useEffect(() => {
    if (isEmpty(sellerApi)) return;
    if (item?.metadata?.name === sellerApi?.name && firstOpen) {
      setTrue();
      setFalse();
      delay(() => {
        const current = document.getElementById("active-server");
        current?.scrollIntoView({ behavior: "smooth", block: "end" });
      }, 1000);
    }
  }, [sellerApi]);

  const baseSpec = useMemo(() => {
    try {
      const encoded = item?.facets?.baseSpec?.content;
      if (!encoded) return undefined;
      const yamlContent = extractOpenApiStrings(decode(encoded));
      return jsYaml.load(yamlContent);
    } catch (error) {
      return "";
    }
  }, [item]);

  const [resolvedSpec, setResolvedSpec] = useState<any>();

  const load = async (spec: Record<string, any>) => {
    try {
      const result = await swaggerClient.resolve({ spec });
      setResolvedSpec(result.spec);
    } catch (error) {
      notification.error({
        message: "Can not load information from API seller",
      });
    }
  };

  useEffect(() => {
    if (!baseSpec) return;
    load(baseSpec);
  }, [baseSpec]);

  useEffect(() => {
    if (isOneItem) {
      setTrue();
    }
  }, [isOneItem]);

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
      setSelectedServer(serverKey);
    }
    const listSpec: any[] = [];
    Object.entries(get(resolvedSpec, "paths", [])).forEach(
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
    setSelectedServer(serverKey);
  };

  const selectedAPIs = useMemo(() => {
    if (isEmpty(searchValue)) {
      return item?.facets?.selectedAPIs;
    }
    return item?.facets?.selectedAPIs?.filter((api: string) =>
      api?.toLocaleLowerCase?.().includes(searchValue?.toLocaleLowerCase?.())
    );
  }, [searchValue]);

  const getDescription = useCallback(
    (path: string, method: string) => {
      const item = get(resolvedSpec, `paths.${path}.${method}`, {
        summary: "",
      });
      return item.summary;
    },
    [resolvedSpec]
  );

  return (
    <>
      <Flex justifyContent="space-between">
        <Flex
          justifyContent="flex-start"
          gap={8}
          alignItems="center"
          onClick={toggleOpen}
          style={{ cursor: "pointer" }}
        >
          {isOpen ? (
            <DownOutlined style={{ fontSize: 10 }} />
          ) : (
            <RightOutlined style={{ fontSize: 10 }} />
          )}
          <Text.NormalMedium>{get(item, "metadata.name")}</Text.NormalMedium>
        </Flex>
      </Flex>
      <Flex
        flexDirection="column"
        gap={12}
        alignItems="flex-start"
        justifyContent="flex-start"
        style={{ width: "100%" }}
      >
        {isOpen && (
          <div className={styles.search}>
            <Input.Search
              placeholder="Search"
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              allowClear
            />
          </div>
        )}
        {isOpen &&
          selectedAPIs?.map((key: string) => {
            const [url, method] = key.split(" ");
            const active = selectedAPI
              ? item?.metadata?.name === selectedAPI?.name &&
                url === selectedAPI?.url &&
                method === selectedAPI?.method
              : item?.metadata?.name === sellerApi?.name &&
                url === sellerApi?.url &&
                method === sellerApi?.method;
            return (
              <div
                id={active ? "active-server" : ""}
                className={clsx(styles.card, {
                  [styles.active]: active,
                })}
                onClick={() => onSelect(key)}
                role="none"
                key={key}
              >
                <Flex
                  justifyContent="flex-start"
                  gap={12}
                  style={{ marginBottom: 10 }}
                >
                  <RequestMethod method={method} noSpace />
                  <Tooltip title={url}>
                    <Typography.Text ellipsis={{ tooltip: true }}>
                      {url}
                    </Typography.Text>
                  </Tooltip>
                </Flex>
                <Typography.Text
                  style={{ fontSize: 14, color: "#00000073" }}
                  ellipsis={{ tooltip: true }}
                >
                  {getDescription(url, method)}
                </Typography.Text>
              </div>
            );
          })}
      </Flex>
    </>
  );
};

const SelectAPI = ({
  isRequiredMapping = true,
}: {
  isRequiredMapping?: boolean;
}) => {
  const { currentProduct } = useAppStore();
  const [selectedAPI, setSelectedAPI] = useState<any>();
  const [selectedServer, setSelectedServer] = useState<string>("");
  const {
    setSellerApi,
    setServerKey,
    sellerApi,
    query,
    setResponseMapping,
    setRequestMapping,
    setListMappingStateResponse,
  } = useNewApiMappingStore();
  const navigate = useNavigate();
  const { data: dataList, isLoading } =
    useGetComponentListAPISpec(currentProduct);
  const queryData = JSON.parse(query ?? "{}");

  const { mappers } = useGetApiSpec(
    currentProduct,
    queryData?.targetMapperKey ?? ""
  );

  const handleMapSave = () => {
    setSellerApi(selectedAPI);
    setServerKey(selectedServer);
    setSelectedAPI(undefined);
    setSelectedServer("");
  };

  const resetMapping = useCallback(() => {
    if (mappers?.request) {
      const newApiRequest = cloneDeep(mappers?.request)
        .filter((rm: any) => !rm.customizedField)
        .map((rm: any) => ({
          ...rm,
          target: "",
          targetLocation: "",
        }));
      const newApiResponse = cloneDeep(mappers?.response).map((rm: any) => ({
        ...rm,
        sourceLocation: undefined,
        source: undefined,
        valueMapping: undefined,
      }));
      setListMappingStateResponse([]);
      setRequestMapping(newApiRequest);
      setResponseMapping(newApiResponse);
    }
  }, [mappers?.request, mappers?.response]);

  const handleOK = () => {
    if (!isRequiredMapping) {
      return;
    }
    if (isEmpty(sellerApi)) {
      handleMapSave();
      return;
    }
    resetMapping();
    handleMapSave();
  };
  const itemLength = dataList?.data?.length ?? 0;

  return (
    <div className={styles.root}>
      <div className={styles.header}>
        <Text.NormalLarge>Select Seller API</Text.NormalLarge>
      </div>
      <Spin spinning={isLoading} className={styles.loading}>
        <div className={styles.container}>
          <div className={styles.content}>
            {!isRequiredMapping && (
              <Text.LightMedium>Not required.</Text.LightMedium>
            )}
            {isRequiredMapping &&
              dataList?.data?.map((item: IComponent, index: number) => (
                <APIItem
                  item={item}
                  key={item.id}
                  isOneItem={itemLength === 1 && index === 0}
                  setSellerApi={setSelectedAPI}
                  selectedAPI={selectedAPI}
                  setSelectedServer={setSelectedServer}
                />
              ))}
            {isRequiredMapping && !isLoading && isEmpty(dataList?.data) && (
              <div className={styles.empty}>
                <Flex flexDirection="column" gap={24}>
                  <EmptyIcon />
                  <Flex gap={9} flexDirection="column">
                    <Text.NormalMedium>
                      No seller API server. Please go to set up Seller API
                      Server
                    </Text.NormalMedium>
                    <Text.LightMedium>
                      Register seller API server by uploading all the
                      information gathered in investigation stage so that MEF
                      LSO Sonata Adapter can trigger it in sonata API request by
                      buyer side.
                    </Text.LightMedium>
                    <Button
                      type="primary"
                      onClick={() =>
                        navigate(`/component/${currentProduct}/new`)
                      }
                    >
                      Seller API Setup
                    </Button>
                  </Flex>
                </Flex>
              </div>
            )}
          </div>
        </div>
      </Spin>
      <Flex
        justifyContent="flex-end"
        style={{ padding: 14, borderTop: "1px solid #F0F0F0 " }}
      >
        <Button
          disabled={isEmpty(selectedAPI) || !isRequiredMapping}
          type="primary"
          onClick={handleOK}
          style={
            isRequiredMapping
              ? {}
              : { background: "#2962FF", opacity: 0.4, color: "#fff" }
          }
        >
          OK
        </Button>
      </Flex>
    </div>
  );
};

export default SelectAPI;
