import ServerIcon from "@/assets/server-icon.svg";
import Flex from "@/components/Flex";
import Text from "@/components/Text";
import { useGetComponentList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { COMPONENT_KIND_API_TARGET_SPEC } from "@/utils/constants/product";
import { IComponent } from "@/utils/types/product.type";
import {
  DownOutlined,
  ExclamationCircleOutlined,
  RightOutlined,
} from "@ant-design/icons";
import {
  Button,
  Input,
  Modal,
  Spin,
  Tooltip,
  Typography,
  notification,
} from "antd";
import clsx from "clsx";
import jsYaml from "js-yaml";
import { cloneDeep, get, isEmpty } from "lodash";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import swaggerClient from "swagger-client";
import { useBoolean } from "usehooks-ts";
import styles from "./index.module.scss";
import useGetApiSpec from "../useGetApiSpec";
import EmptyIcon from "@/assets/newAPIServer/empty.svg";
import RequestMethod from "@/components/Method";

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

  const baseSpec = useMemo(() => {
    try {
      const encoded = item?.facets?.baseSpec?.content;
      if (!encoded) return undefined;
      const yamlContent = atob(encoded.slice(31))
        .replace(/(â)/g, "")
        .replace(/(â)/g, "");
      return jsYaml.load(yamlContent);
    } catch (error) {
      return "";
    }
  }, [item]);

  const [resolvedSpec, setResolvedSpec] = useState<any>();

  useEffect(() => {
    if (!baseSpec) return;
    (async () => {
      try {
        const result = await swaggerClient.resolve({ spec: baseSpec });
        setResolvedSpec(result.spec);
      } catch (error) {
        notification.error({
          message: "Can not load information from API seller",
        });
      }
    })();
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
                className={clsx(styles.card, {
                  [styles.active]: active,
                })}
                onClick={() => onSelect(key)}
                role="none"
                key={key}
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
                  <RequestMethod method={method} noSpace />
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
    </>
  );
};

const SelectAPI = ({ save }: { save: () => Promise<true | undefined> }) => {
  const { currentProduct } = useAppStore();
  const [selectedAPI, setSelectedAPI] = useState<any>();
  const [selectedServer, setSelectedServer] = useState<string>("");
  const {
    setSellerApi,
    setServerKey,
    sellerApi,
    reset,
    query,
    setResponseMapping,
    setRequestMapping,
  } = useNewApiMappingStore();
  const navigate = useNavigate();
  const { data: dataList, isLoading } = useGetComponentList(currentProduct, {
    kind: COMPONENT_KIND_API_TARGET_SPEC,
    size: 1000,
  });

  const { mappers } = useGetApiSpec(currentProduct, query ?? "{}");

  const handleMapSave = () => {
    setSellerApi(selectedAPI);
    setServerKey(selectedServer);
    setSelectedAPI(undefined);
    setSelectedServer("");
  };

  const resetMapping = () => {
    reset();
    const newApiRequest = cloneDeep(mappers?.request)
      .filter((rm: any) => !!rm.requiredMapping)
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
    setRequestMapping(newApiRequest);
    setResponseMapping(newApiResponse);
  };

  const handleOK = () => {
    if (isEmpty(sellerApi)) {
      handleMapSave();
      return;
    }
    Modal.confirm({
      width: 500,
      icon: <ExclamationCircleOutlined />,
      title:
        "You are going to switch to another seller API, do you want to save the mappings of current selected API?",
      content: (
        <Text.LightMedium>
          Select yes will save the mappings and switch to another API.
          <br />
          Select no will drop the mappings and switch.
        </Text.LightMedium>
      ),
      footer: (_, { CancelBtn, OkBtn }) => (
        <Flex gap={8} justifyContent="flex-end">
          <CancelBtn />
          <Button
            type="default"
            onClick={() => {
              resetMapping();
              handleMapSave();
              Modal.destroyAll();
            }}
          >
            Drop and switch
          </Button>
          <OkBtn />
        </Flex>
      ),
      okButtonProps: {
        type: "primary",
      },
      okText: "Save and switch",
      onOk: async () => {
        await save();
        resetMapping();
        handleMapSave();
        Modal.destroyAll();
      },
      cancelButtonProps: {
        type: "text",
        style: {
          color: "#1890FF",
        },
      },
    });
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
            {dataList?.data?.map((item: IComponent, index: number) => (
              <APIItem
                item={item}
                key={item.id}
                isOneItem={itemLength === 1 && index === 0}
                setSellerApi={setSelectedAPI}
                selectedAPI={selectedAPI}
                setSelectedServer={setSelectedServer}
              />
            ))}
            {!isLoading && isEmpty(dataList?.data) && (
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
          disabled={isEmpty(selectedAPI)}
          type="primary"
          onClick={handleOK}
        >
          OK
        </Button>
      </Flex>
    </div>
  );
};

export default SelectAPI;
