import { Alert } from "@/components/Alert";
import Flex from "@/components/Flex";
import StepBar from "@/components/StepBar";
import { Text } from "@/components/Text";
import { PRODUCT_CACHE_KEYS } from "@/hooks/product";
import { usePathQuery } from "@/hooks/usePathQuery";
import { useAppStore } from "@/stores/app.store";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EStep } from "@/utils/constants/common";
import buildInitListMapping from "@/utils/helpers/buildInitListMapping";
import { isElementInViewport } from "@/utils/helpers/html";
import { queryClient } from "@/utils/helpers/reactQuery";
import { EnumRightType } from "@/utils/types/common.type";
import { InfoCircleOutlined } from "@ant-design/icons";
import { Tabs, TabsProps } from "antd";
import { isEmpty, uniqBy } from "lodash";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import HeaderMapping from "./components/HeaderMapping";
import NotRequired from "./components/NotRequired";
import RequestMapping from "./components/RequestMapping";
import ResponseMapping from "./components/ResponseMapping";
import { RightSide } from "./components/RightSide";
import useGetApiSpec from "./components/useGetApiSpec";
import useGetDefaultSellerApi from "./components/useGetDefaultSellerApi";
import styles from "./index.module.scss";

const NewAPIMapping = ({
  isRequiredMapping,
}: {
  isRequiredMapping: boolean;
}) => {
  const pathQuery = usePathQuery();
  const { currentProduct } = useAppStore();
  const { activeTab, setActiveTab } = useMappingUiStore();
  const {
    query,
    rightSide,
    serverKey,
    requestMapping,
    rightSideInfo,
    sellerApi,
    setRequestMapping,
    setRightSide,
    setResponseMapping,
    setSellerApi,
    setServerKey,
    setListMappingStateResponse,
    setListMappingStateRequest,
    setRightSideInfo,
    // setErrors,
  } = useNewApiMappingStore();
  const queryData = useMemo(() => JSON.parse(query ?? "{}"), [query]);

  const [firstTimeLoad, setFirstTimeLoad] = useState(true);
  const [activeKey, setActiveKey] = useState<string | string[]>("0");
  const [step, setStep] = useState(0);

  const {
    serverKeyInfo,
    mappers,
    loadingMapper,
    resetMapping,
    resetResponseMapping,
    jsonSpec,
  } = useGetApiSpec(currentProduct, queryData.targetMapperKey ?? "");

  const { sellerApi: defaultSellerApi, serverKey: defaultServerKey } =
    useGetDefaultSellerApi(currentProduct, serverKeyInfo as any);

  const [firstTimeLoadSellerAPI, setFirstTimeLoadSellerAPI] = useState(true);

  const ref = useRef<any>();

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
    <main className={styles.container}>
      {/* User guide */}
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
        <div ref={ref} className={styles.newContent}>
          {upgradingVersion && (
            <Alert
              type="warning"
              style={{ marginBottom: 16 }}
              description={
                <>
                  Upgrading to{" "}
                  <Link to={`/mapping-template?version=${upgradingVersion}`}>
                    Api mapping template {upgradingVersion}
                  </Link>
                </>
              }
            />
          )}
          <Flex gap={12} className={styles.mainWrapper} alignItems="start">
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

              <HeaderMapping disabled={!isRequiredMapping} mappers={mappers} />
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
        </div>
      </Flex>
    </main>
  );
};

export default NewAPIMapping;
