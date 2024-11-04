import RollbackIcon from "@/assets/newAPIMapping/Rollback.svg";
import { Alert } from "@/components/Alert";
import DeployStage from "@/components/DeployStage";
import Flex from "@/components/Flex";
import StepBar from "@/components/StepBar";
import { Text } from "@/components/Text";
import {
  PRODUCT_CACHE_KEYS,
  useGetLatestRunningList,
  useUpdateTargetMapper,
} from "@/hooks/product";
import { usePathQuery } from "@/hooks/usePathQuery";
import useSize from "@/hooks/useSize";
import { useAppStore } from "@/stores/app.store";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EStep } from "@/utils/constants/common";
import buildInitListMapping from "@/utils/helpers/buildInitListMapping";
import { isElementInViewport } from "@/utils/helpers/html";
import { queryClient } from "@/utils/helpers/reactQuery";
import { EnumRightType } from "@/utils/types/common.type";
import { IMappers } from "@/utils/types/component.type";
import { InfoCircleOutlined } from "@ant-design/icons";
import { Button, Tabs, TabsProps, Tooltip, notification } from "antd";
import dayjs from "dayjs";
import {
  chain,
  cloneDeep,
  flatMap,
  get,
  isEmpty,
  reduce,
  uniqBy,
} from "lodash";
import { useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { useSessionStorage } from "usehooks-ts";
import DeployHistory from "./components/DeployHistory";
import { Deployment } from "./components/Deployment";
import DeploymentInfo from "./components/DeploymentInfo";
import HeaderMapping from "./components/HeaderMapping";
import NotRequired from "./components/NotRequired";
import RequestMapping from "./components/RequestMapping";
import ResponseMapping, { IMapping } from "./components/ResponseMapping";
import { RightSide } from "./components/RightSide";
import useGetApiSpec from "./components/useGetApiSpec";
import useGetDefaultSellerApi from "./components/useGetDefaultSellerApi";
// import { validateMappers } from "./helper";
import styles from "./index.module.scss";

enum EMainTab {
  mapping = "mapping",
  deploy = "deploy",
}

const collapsedStyle = { maxWidth: `calc(100vw - 462px)` };

const NewAPIMapping = ({
  refetch,
  isRequiredMapping,
}: {
  refetch?: () => void;
  isRequiredMapping: boolean;
}) => {
  const pathQuery = usePathQuery();
  const [collapsed] = useSessionStorage("collapsed", false);
  const { currentProduct } = useAppStore();
  const { activeTab, setActiveTab } = useMappingUiStore();
  const {
    query,
    rightSide,
    serverKey,
    requestMapping,
    responseMapping,
    rightSideInfo,
    sellerApi,
    setRequestMapping,
    setRightSide,
    setResponseMapping,
    setSellerApi,
    setServerKey,
    setListMappingStateResponse,
    listMappingStateResponse,
    setListMappingStateRequest,
    listMappingStateRequest,
    setRightSideInfo,
    // setErrors,
  } = useNewApiMappingStore();
  const queryData = JSON.parse(query ?? "{}");

  const [firstTimeLoad, setFirstTimeLoad] = useState(true);
  const [activeKey, setActiveKey] = useState<string | string[]>("0");
  const [step, setStep] = useState(0);

  const { mutateAsync: updateTargetMapper, isPending } =
    useUpdateTargetMapper();
  const {
    serverKeyInfo,
    mappers,
    mapperResponse,
    loadingMapper,
    metadataKey,
    resetMapping,
    resetResponseMapping,
    jsonSpec,
    refreshMappingDetail,
  } = useGetApiSpec(currentProduct, queryData.targetMapperKey ?? "");

  const { sellerApi: defaultSellerApi, serverKey: defaultServerKey } =
    useGetDefaultSellerApi(currentProduct, serverKeyInfo as any);

  const [mainTabKey, setMainTabKey] = useState<string>(EMainTab.mapping);
  const [firstTimeLoadSellerAPI, setFirstTimeLoadSellerAPI] = useState(true);

  const ref = useRef<any>();
  const size = useSize(ref);
  const { data: runningDeploymentData, isFetching: isFetchingDeploymentData } =
    useGetLatestRunningList(currentProduct, queryData?.targetMapperKey);

  useEffect(() => {
    if (!sellerApi && defaultSellerApi && firstTimeLoadSellerAPI) {
      setSellerApi(defaultSellerApi);
      setFirstTimeLoadSellerAPI(false);
    }
  }, [sellerApi, setSellerApi, defaultSellerApi, firstTimeLoadSellerAPI]);

  useEffect(() => {
    if (!serverKey && defaultServerKey) {
      setServerKey(defaultServerKey);
    }
  }, [defaultServerKey, serverKey, setServerKey]);

  useEffect(() => {
    if (firstTimeLoad && !isEmpty(mappers?.request)) {
      setListMappingStateRequest(
        buildInitListMapping(mappers?.request as any, "request")
      );
      setRequestMapping(resetMapping() ?? []);
    }
  }, [mappers?.request, firstTimeLoad]);

  useEffect(() => {
    if (firstTimeLoad && !isEmpty(mappers?.response)) {
      setResponseMapping(resetResponseMapping());
      setListMappingStateResponse(
        buildInitListMapping(mappers?.response as any, "response")
      );
      setFirstTimeLoad(false);
    }
  }, [mappers?.response, firstTimeLoad]);

  useEffect(() => {
    return () => {
      queryClient.removeQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_component_list_v2],
      });
    };
  }, []);

  useEffect(() => {
    if (rightSide === EnumRightType.SelectSellerAPI) {
      setStep(0);
      setActiveKey("0");
    }
    if (
      rightSide === EnumRightType.AddSonataProp ||
      rightSide === EnumRightType.AddSellerProp
    ) {
      setStep(1);
      setActiveKey("1");
    }
  }, [rightSide]);

  const items: TabsProps["items"] = [
    {
      key: "request",
      label: "Request mapping",
      children: isRequiredMapping ? <RequestMapping /> : <NotRequired />,
    },
    {
      key: "response",
      label: "Response mapping",
      children: isRequiredMapping ? <ResponseMapping /> : <NotRequired />,
      disabled: loadingMapper,
    },
  ];

  const handleSelectSonataProp = useCallback(
    (selected: any) => {
      if (rightSideInfo?.method === "add") {
        const updatedMapping = uniqBy(
          [
            ...requestMapping,
            {
              sourceLocation: selected.location,
              source: selected.name,
              title: selected.title,
            },
          ],
          (rm) =>
            `${rm.source}_${rm.sourceLocation}_${rm.target}_${rm.targetLocation}`
        );
        setRequestMapping(updatedMapping);
      }
      if (rightSideInfo?.method === "update") {
        const updatedMapping = uniqBy(
          requestMapping.map((rm) => {
            if (
              rm.source === rightSideInfo?.previousData?.source &&
              rm.sourceLocation === rightSideInfo?.previousData?.sourceLocation
            ) {
              return {
                ...rm,
                source: selected.name,
                sourceLocation: selected.location,
              };
            }
            return rm;
          }),
          (rm) =>
            `${rm.source}_${rm.sourceLocation}_${rm.target}_${rm.targetLocation}`
        );
        setRequestMapping(updatedMapping);
        setRightSideInfo({
          ...rightSideInfo,
          previousData: {
            ...rightSideInfo.previousData,
            source: selected.name,
            sourceLocation: selected.location,
          },
        });
        const currentDom = document.getElementById(
          JSON.stringify(rightSideInfo.previousData)
        );
        if (currentDom && !isElementInViewport(currentDom, 130)) {
          currentDom?.scrollIntoView({ behavior: "smooth", block: "center" });
        }
      }
    },
    [rightSideInfo, requestMapping, setRequestMapping]
  );

  const handleSelectSellerProp = useCallback(
    (selected: any) => {
      const updatedMapping = uniqBy(
        requestMapping.map((rm) => {
          if (
            rm.source === rightSideInfo?.previousData?.source &&
            rm.sourceLocation === rightSideInfo?.previousData?.sourceLocation &&
            rm.name === rightSideInfo?.previousData?.name
          ) {
            return {
              ...rm,
              target: selected.name,
              targetLocation: selected.location,
            };
          }
          return rm;
        }),
        (rm) =>
          `${rm.source}_${rm.sourceLocation}_${rm.target}_${rm.targetLocation}`
      );
      setRequestMapping(updatedMapping);
      setRightSideInfo({
        ...rightSideInfo,
        previousData: {
          ...rightSideInfo.previousData,
          target: selected.name,
          targetLocation: selected.location,
        },
      });
      const currentDom = document.getElementById(
        JSON.stringify(rightSideInfo.previousData)
      );
      if (currentDom && !isElementInViewport(currentDom, 130)) {
        currentDom?.scrollIntoView({ behavior: "smooth", block: "center" });
      }
    },
    [rightSideInfo, requestMapping, setRequestMapping]
  );

  const transformListMappingItem = (
    item: IMapping[],
    type: "request" | "response"
  ) => {
    return chain(item)
      .groupBy("name")
      .map((items, name) => ({
        name,
        valueMapping: flatMap(items, (item) =>
          // item?.to?.map((to) => ({ [to]: item.from }))
          type === "request"
            ? [{ [item.from as string]: item.to?.[0] }]
            : item?.to?.map((to) => ({ [to]: item.from }))
        ),
      }))
      .value();
  };

  const handleSave = async (callback?: () => void) => {
    try {
      // @TODO: temporarily remove for demo
      // Validate properties name and location
      // const { requestIds, responseIds, errorMessage } = validateMappers({
      //   request: requestMapping,
      //   response: responseMapping,
      // });
      // setErrors({ requestIds, responseIds });

      // if (errorMessage) {
      //   notification.error({ message: errorMessage });

      //   return;
      // }

      const newDataResponse = transformListMappingItem(
        listMappingStateResponse,
        "response"
      );
      const newDataRequest = transformListMappingItem(
        listMappingStateRequest,
        "request"
      );

      let newResponse = cloneDeep(responseMapping);
      if (!isEmpty(newDataResponse)) {
        newDataResponse.forEach((it) => {
          newResponse = newResponse.map((rm) => {
            if (rm.name === it.name) {
              rm.valueMapping = reduce(
                it.valueMapping,
                (acc, obj) => ({ ...acc, ...obj }),
                {}
              );
            }
            return rm;
          });
        });
      }
      let newRequest = cloneDeep(requestMapping);
      if (!isEmpty(newDataRequest)) {
        newDataRequest.forEach((it) => {
          newRequest = newRequest.map((rm) => {
            if (rm.name === it.name) {
              rm.valueMapping = reduce(
                it.valueMapping,
                (acc, obj) => ({ ...acc, ...obj }),
                {}
              );
            }
            return rm;
          });
        });
      }

      const mappers: IMappers = {
        request: newRequest.map((rm) => ({
          ...rm,
          target: get(rm, "target", ""),
          source: get(rm, "source", ""),
          targetLocation:
            isEmpty(rm?.target) && rm?.targetLocation === "HYBRID"
              ? ""
              : get(rm, "targetLocation", ""),
          sourceLocation: get(rm, "sourceLocation", ""),
          requiredMapping: Boolean(rm.requiredMapping),
          id: undefined, // Omit id from patch payload
        })),
        response: newResponse.map((rm) => ({
          ...rm,
          targetLocation: get(rm, "targetLocation", ""),
          sourceLocation: get(rm, "sourceLocation", ""),
          target: get(rm, "target", ""),
          source: get(rm, "source", ""),
          requiredMapping: Boolean(rm.requiredMapping),
          id: undefined, // Omit id from patch payload
        })),
      };

      const data = cloneDeep(mapperResponse)!;
      data.facets.endpoints[0] = {
        ...data.facets.endpoints[0],
        serverKey: serverKey as any,
        method: sellerApi.method,
        path: sellerApi.url,
        mappers,
      };

      const res = await updateTargetMapper({
        productId: currentProduct,
        componentId: data.metadata.id,
        data,
      } as any);
      notification.success({ message: res.message });
      refreshMappingDetail();
      setStep(1);
      setActiveKey("1");
      callback && callback();
      return true;
    } catch (error) {
      notification.error({
        message: get(
          error,
          "reason",
          get(error, "message", "Error on creating/updating mapping")
        ),
      });
    }
  };

  const handleRevert = () => {
    setRequestMapping(resetMapping() ?? []);
    setResponseMapping(mappers?.response);
    setListMappingStateResponse(
      buildInitListMapping(mappers?.response as any, "response")
    );
    setActiveTab("request");
  };

  const handleTabSwitch = useCallback(
    (tabName: string) => {
      if (tabName === "response" && !isEmpty(sellerApi)) {
        setRightSide(EnumRightType.AddSellerResponse);
      } else {
        setRightSide(EnumRightType.AddSonataProp);
      }
      setActiveTab(tabName);
    },
    [sellerApi, setActiveTab, setRightSide]
  );

  const upgradingVersion = pathQuery.get("version");

  return (
    <Flex className={styles.container}>
      <StepBar
        type={EStep.MAPPING}
        currentStep={step}
        activeKey={activeKey}
        setActiveKey={setActiveKey}
      />

      <Flex
        flexDirection="column"
        justifyContent="flex-start"
        className={styles.newMainWrapper}
      >
        <Flex
          justifyContent="space-between"
          style={{ width: "100%", paddingBottom: 16 }}
        >
          <Tabs
            id="tab-mapping"
            activeKey={mainTabKey}
            onChange={setMainTabKey}
            items={[
              {
                label: (
                  <Flex gap={4} alignItems="center">
                    Mapping
                    {queryData.mappingStatus === "incomplete" && (
                      <Tooltip title="Incomplete mapping">
                        <InfoCircleOutlined style={{ color: "#FAAD14" }} />
                      </Tooltip>
                    )}
                  </Flex>
                ),
                key: EMainTab.mapping,
              },
              { label: "Deploy history", key: EMainTab.deploy },
            ]}
          />
          <DeploymentInfo
            runningData={runningDeploymentData as any}
            loading={isFetchingDeploymentData}
          />
        </Flex>
        {mainTabKey === EMainTab.mapping && (
          <Flex className={styles.breadcrumb} justifyContent="space-between">
            <Flex className={styles.infoBox}>
              {queryData?.lastDeployedAt && (
                <Deployment
                  deploymentData={runningDeploymentData}
                  loading={isFetchingDeploymentData}
                />
              )}
            </Flex>
            <Flex
              justifyContent="flex-end"
              gap={8}
              className={styles.bottomWrapper}
            >
              {isRequiredMapping && (
                <>
                  <Tooltip title="Restore">
                    <Button
                      disabled={!isRequiredMapping}
                      className={styles.revertButton}
                      onClick={handleRevert}
                    >
                      <RollbackIcon />
                    </Button>
                  </Tooltip>
                  <Tooltip
                    title={
                      queryData?.updatedAt
                        ? dayjs
                            .utc(queryData?.updatedAt)
                            .local()
                            .format("YYYY-MM-DD HH:mm:ss")
                        : undefined
                    }
                  >
                    <Button
                      disabled={!isRequiredMapping}
                      data-testid="btn-save"
                      type="default"
                      onClick={() => handleSave(refetch)}
                      loading={isPending}
                      className={styles.btnSave}
                    >
                      Save
                    </Button>
                  </Tooltip>
                  <Button type="default">Compare</Button>
                </>
              )}
              <DeployStage
                inComplete={queryData.mappingStatus === "incomplete"}
                diffWithStage={queryData.diffWithStage}
                metadataKey={metadataKey as any}
              />
            </Flex>
          </Flex>
        )}
        <div
          ref={ref}
          className={styles.newContent}
          style={collapsed ? collapsedStyle : {}}
        >
          {upgradingVersion && (
            <Alert
              type="warning"
              style={{ marginBottom: 16 }}
              description={
                <>
                  Upgrading to{" "}
                  <Link to={`/mapping-template-v2?version=${upgradingVersion}`}>
                    Api mapping template {upgradingVersion}
                  </Link>
                </>
              }
            />
          )}

          {mainTabKey === EMainTab.mapping ? (
            <Flex
              gap={12}
              className={styles.mainWrapper}
              style={collapsed ? collapsedStyle : {}}
            >
              <div className={styles.center}>
                {!isRequiredMapping && (
                  <Flex
                    justifyContent="flex-start"
                    gap={10}
                    className={styles.isRequiredMapping}
                    alignItems="center"
                  >
                    <InfoCircleOutlined style={{ color: "#00000073" }} />
                    <Text.LightSmall color="#000000D9">
                      This mapping is not needed, all the data will be queried
                      from Adapter layer. This end point is able to deploy.
                    </Text.LightSmall>
                  </Flex>
                )}

                <HeaderMapping
                  disabled={!isRequiredMapping}
                  mappers={mappers}
                />
                <Tabs
                  items={items}
                  activeKey={activeTab}
                  onChange={handleTabSwitch}
                />
              </div>

              {isRequiredMapping && (
                <div className={styles.right}>
                  <RightSide
                    rightSide={Number(rightSide)}
                    isRequiredMapping={isRequiredMapping}
                    method={queryData?.method}
                    jsonSpec={jsonSpec}
                    handleSelectSellerProp={handleSelectSellerProp}
                    handleSelectSonataProp={handleSelectSonataProp}
                  />
                </div>
              )}
            </Flex>
          ) : (
            <div className={styles.history}>
              <DeployHistory
                targetMapperKey={queryData.targetMapperKey}
                scrollHeight={get(size, "height", 0) + 70}
              />
            </div>
          )}
        </div>
      </Flex>
    </Flex>
  );
};

export default NewAPIMapping;
