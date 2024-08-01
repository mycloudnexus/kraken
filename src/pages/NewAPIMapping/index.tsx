import {
  forwardRef,
  useCallback,
  useEffect,
  useImperativeHandle,
  useState,
} from "react";
import { InfoCircleOutlined } from "@ant-design/icons";
import RollbackIcon from "@/assets/newAPIMapping/Rollback.svg";
import { Button, Tabs, TabsProps, Tag, Tooltip, notification } from "antd";
import {
  chain,
  cloneDeep,
  flatMap,
  get,
  isEmpty,
  reduce,
  uniqBy,
} from "lodash";
import Flex from "@/components/Flex";
import StepBar from "@/components/StepBar";
import RequestMapping from "./components/RequestMapping";
import ResponseMapping from "./components/ResponseMapping";
import RightAddSellerProp from "./components/RightAddSellerProp";
import RightAddSonataProp from "./components/RightAddSonataProp";
import SelectAPI from "./components/SelectAPI";
import SelectResponseProperty from "./components/SelectResponseProperty";
import useGetApiSpec from "./components/useGetApiSpec";
import useGetDefaultSellerApi from "./components/useGetDefaultSellerApi";
import HeaderMapping from "./components/HeaderMapping";
import DeployStandardAPI from "@/components/DeployStandardAPI";
import { PRODUCT_CACHE_KEYS, useUpdateTargetMapper } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EStep } from "@/utils/constants/common";
import { EnumRightType } from "@/utils/types/common.type";
import { toDateTime } from "@/libs/dayjs";
import { queryClient } from "@/utils/helpers/reactQuery";
import styles from "./index.module.scss";
import buildInitListMapping from "@/utils/helpers/buildInitListMapping";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import Text from "@/components/Text";

type Props = {
  rightSide: number;
  jsonSpec: any;
  method: string;
  handleSelectSonataProp: (value: any) => void;
  handleSelectSellerProp: (value: any) => void;
  isRequiredMapping: boolean;
};

const RightSide = ({
  rightSide,
  jsonSpec,
  method,
  handleSelectSonataProp,
  handleSelectSellerProp,
  isRequiredMapping,
}: Props) => {
  if (!isRequiredMapping) {
    return <SelectAPI isRequiredMapping={isRequiredMapping} />;
  }
  switch (rightSide) {
    case EnumRightType.AddSonataProp:
      return (
        <RightAddSonataProp
          spec={jsonSpec}
          method={method}
          onSelect={handleSelectSonataProp}
        />
      );
    case EnumRightType.SelectSellerAPI:
      return <SelectAPI />;

    case EnumRightType.AddSellerProp:
      return <RightAddSellerProp onSelect={handleSelectSellerProp} />;
    case EnumRightType.AddSellerResponse:
      return <SelectResponseProperty />;
    default:
      return <></>;
  }
};

const NewAPIMapping = forwardRef(
  (
    {
      refetch,
      isRequiredMapping,
    }: { refetch?: () => void; isRequiredMapping: boolean },
    ref
  ) => {
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
      setRightSideInfo,
      setResponseMapping,
      setSellerApi,
      setServerKey,
      setListMappingStateResponse,
      listMappingStateResponse,
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
      jsonSpec,
    } = useGetApiSpec(currentProduct, queryData.targetMapperKey ?? "");
    const { sellerApi: defaultSellerApi, serverKey: defaultServerKey } =
      useGetDefaultSellerApi(currentProduct, serverKeyInfo);

    useEffect(() => {
      if (!sellerApi && defaultSellerApi) {
        setSellerApi(defaultSellerApi);
      }
    }, [sellerApi, setSellerApi, defaultSellerApi]);

    useEffect(() => {
      if (!serverKey && defaultServerKey) {
        setServerKey(defaultServerKey);
      }
    }, [defaultServerKey, serverKey, setServerKey]);

    useEffect(() => {
      if (firstTimeLoad && !isEmpty(mappers?.request)) {
        setRequestMapping(resetMapping() ?? []);
      }
    }, [mappers?.request, firstTimeLoad, setRequestMapping, resetMapping]);

    useEffect(() => {
      if (firstTimeLoad && !isEmpty(mappers?.response)) {
        setResponseMapping(mappers?.response);
        setListMappingStateResponse(buildInitListMapping(mappers?.response));
        setFirstTimeLoad(false);
      }
    }, [
      mappers?.response,
      firstTimeLoad,
      setResponseMapping,
      setListMappingStateResponse,
    ]);

    useEffect(() => {
      return () => {
        queryClient.removeQueries({
          queryKey: [PRODUCT_CACHE_KEYS.get_component_list],
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
        children: isRequiredMapping ? (
          <RequestMapping />
        ) : (
          <Text.LightMedium>Not required.</Text.LightMedium>
        ),
      },
      {
        key: "response",
        label: "Response mapping",
        children: isRequiredMapping ? (
          <ResponseMapping />
        ) : (
          <Text.LightMedium>Not required.</Text.LightMedium>
        ),
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
                rm.sourceLocation ===
                  rightSideInfo?.previousData?.sourceLocation
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
        }
        setRightSideInfo(undefined);
        setRightSide(undefined);
      },
      [rightSideInfo, requestMapping, setRequestMapping]
    );

    const handleSelectSellerProp = useCallback(
      (selected: any) => {
        const updatedMapping = uniqBy(
          requestMapping.map((rm) => {
            if (
              rm.source === rightSideInfo?.previousData?.source &&
              rm.sourceLocation ===
                rightSideInfo?.previousData?.sourceLocation &&
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
        setRightSideInfo(undefined);
        setRightSide(undefined);
      },
      [rightSideInfo, requestMapping, setRequestMapping]
    );

    const handleSave = async (callback?: () => void) => {
      try {
        const newData = chain(listMappingStateResponse)
          .groupBy("name")
          .map((items, name) => ({
            name,
            valueMapping: flatMap(items, (item) =>
              item?.to?.map((to) => ({ [to]: item.from }))
            ),
          }))
          .value();
        let newResponse = cloneDeep(responseMapping);
        if (!isEmpty(newData)) {
          newData.forEach((it) => {
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

        const data = cloneDeep(mapperResponse);
        data.facets.endpoints[0] = {
          ...data.facets.endpoints[0],
          serverKey,
          method: sellerApi.method,
          path: sellerApi.url,
          mappers: {
            request: requestMapping.map((rm) => ({
              ...rm,
              target: get(rm, "target", "")
                ?.replace?.("path.", "")
                .replace?.("query.", "")
                .replace?.("hybrid.", ""),
              source: get(rm, "source", "")
                ?.replace?.("path.", "")
                .replace?.("query.", "")
                .replace?.("hybrid.", ""),
              targetLocation: !isEmpty(get(rm, "target", ""))
                ? get(rm, "targetLocation", "")
                : "",
              sourceLocation: get(rm, "sourceLocation", ""),
              requiredMapping: Boolean(rm.requiredMapping),
            })),
            response: newResponse.map((rm) => ({
              ...rm,
              targetLocation: get(rm, "targetLocation", ""),
              sourceLocation: get(rm, "sourceLocation", ""),
              target: get(rm, "target", ""),
              source: get(rm, "source", ""),
              requiredMapping: Boolean(rm.requiredMapping),
            })),
          },
        };
        const res = await updateTargetMapper({
          productId: currentProduct,
          componentId: data.metadata.id,
          data,
        } as any);
        notification.success({ message: res.message });
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

    const handleRevert = useCallback(() => {
      setRequestMapping(resetMapping() ?? []);
      setResponseMapping(mappers?.response);
      setActiveTab("request");
    }, [
      setRequestMapping,
      resetMapping,
      setResponseMapping,
      mappers?.response,
      setActiveTab,
    ]);

    const handleTabSwitch = useCallback(
      (tabName: string) => {
        if (tabName === "response" && !isEmpty(sellerApi)) {
          setRightSide(EnumRightType.AddSellerResponse);
        }
        setActiveTab(tabName);
      },
      [sellerApi, setActiveTab, setRightSide]
    );

    useImperativeHandle(ref, () => ({
      handleSave,
      handleRevert,
    }));

    return (
      <Flex className={styles.container}>
        <StepBar
          type={EStep.MAPPING}
          currentStep={step}
          activeKey={activeKey}
          setActiveKey={setActiveKey}
        />
        <Flex gap={8} className={styles.mainWrapper}>
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
                  This mapping is not needed, all the data will be queried from
                  Adapter layer. This end point is able to deploy.
                </Text.LightSmall>
              </Flex>
            )}
            <Flex className={styles.breadcrumb} justifyContent="space-between">
              <Flex className={styles.infoBox}>
                {queryData.mappingStatus === "incomplete" && (
                  <Tag bordered={false} color="error">
                    Incomplete
                  </Tag>
                )}
                {queryData?.lastDeployedAt && (
                  <Flex gap={8}>
                    {toDateTime(queryData.lastDeployedAt)}
                    <Tooltip title="Last deployed at">
                      <InfoCircleOutlined
                        style={{ color: "rgba(0, 0, 0, 0.45)" }}
                      />
                    </Tooltip>
                  </Flex>
                )}
              </Flex>
              <Flex
                justifyContent="flex-end"
                gap={8}
                className={styles.bottomWrapper}
              >
                <DeployStandardAPI metadataKey={metadataKey} />
                <Tooltip title="Restore">
                  <Button
                    disabled={!isRequiredMapping}
                    className={styles.revertButton}
                    onClick={handleRevert}
                  >
                    <RollbackIcon />
                  </Button>
                </Tooltip>

                <Button
                  disabled={!isRequiredMapping}
                  data-testid="btn-save"
                  type="primary"
                  onClick={() => handleSave(refetch)}
                  loading={isPending}
                >
                  Save
                </Button>
              </Flex>
            </Flex>
            <HeaderMapping disabled={!isRequiredMapping} />
            <Tabs
              items={items}
              activeKey={activeTab}
              onChange={handleTabSwitch}
            />
          </div>
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
        </Flex>
      </Flex>
    );
  }
);

export default NewAPIMapping;
