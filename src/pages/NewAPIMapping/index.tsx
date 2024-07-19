import Flex from "@/components/Flex";
import StepBar from "@/components/StepBar";
import { PRODUCT_CACHE_KEYS, useUpdateTargetMapper } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { EStep } from "@/utils/constants/common";
import { EnumRightType } from "@/utils/types/common.type";
import { InfoCircleOutlined } from "@ant-design/icons";
import RollbackIcon from "@/assets/newAPIMapping/Rollback.svg";

import { Button, Tabs, TabsProps, Tag, Tooltip, notification } from "antd";
import {
  chain,
  cloneDeep,
  every,
  flatMap,
  get,
  isEmpty,
  keys,
  pickBy,
  reduce,
  uniqBy,
} from "lodash";
import { toDateTime } from "@/libs/dayjs";
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import RequestMapping from "./components/RequestMapping";
import ResponseMapping, { IMapping } from "./components/ResponseMapping";
import RightAddSellerProp from "./components/RightAddSellerProp";
import RightAddSonataProp from "./components/RightAddSonataProp";
import SelectAPI from "./components/SelectAPI";
import SelectResponseProperty from "./components/SelectResponseProperty";
import useGetApiSpec from "./components/useGetApiSpec";
import useGetDefaultSellerApi from "./components/useGetDefaultSellerApi";
import styles from "./index.module.scss";
import { queryClient } from "@/utils/helpers/reactQuery";
import HeaderMapping from "./components/HeaderMapping";
import { IRequestMapping } from "@/utils/types/component.type";
import DeployStandardAPI from "@/components/DeployStandardAPI";

export const buildInitListMapping = (responseMapping: any[]) => {
  let k = 0;
  let list: IMapping[] = [];
  for (const item of responseMapping) {
    if (
      !isEmpty(item.valueMapping) &&
      every(item.valueMapping, (v) => !isEmpty(v))
    ) {
      let res = chain(item.valueMapping)
        .groupBy((value) => value)
        .map((_, from) => {
          k += 1;
          return {
            from,
            to: keys(pickBy(item.valueMapping, (v) => v === from)),
            key: k,
            name: item.name,
          };
        })
        .value();
      list = [...list, ...res];
    }
  }
  return list;
};

const NewAPIMapping = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { currentProduct } = useAppStore();
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
    reset,
    setListMappingStateResponse,
    listMappingStateResponse,
  } = useNewApiMappingStore();
  const { mutateAsync: updateTargetMapper, isPending } =
    useUpdateTargetMapper();
  const [firstTimeLoad, setFirstTimeLoad] = useState(true);
  const queryData = JSON.parse(query ?? "{}");
  const [activeKey, setActiveKey] = useState<string | string[]>("0");
  const [step, setStep] = useState(0);

  const {
    jsonSpec,
    serverKeyInfo,
    mappers,
    mapperResponse,
    loadingMapper,
    componentKey,
  } = useGetApiSpec(currentProduct, query ?? "{}");
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

  const transformTarget = (from: string, fromSrc: string) => {
    switch (fromSrc) {
      case "PATH":
        return from.replace("@{{", `@{{path.`);
      case "QUERY":
        return from.replace("@{{", `@{{query.`);
      default:
        return from;
    }
  };

  const resetMapping = () => {
    return mappers?.request?.map((rm: any) => ({
      ...rm,
      target: transformTarget(rm.target, rm.targetLocation),
      source: transformTarget(rm.source, rm.sourceLocation),
    }));
  };

  useEffect(() => {
    if (firstTimeLoad && !isEmpty(mappers?.request)) {
      setRequestMapping(resetMapping() ?? []);
    }
  }, [mappers?.request, firstTimeLoad]);

  useEffect(() => {
    if (firstTimeLoad && !isEmpty(mappers?.response)) {
      setResponseMapping(mappers?.response);
      setListMappingStateResponse(buildInitListMapping(mappers?.response));
      setFirstTimeLoad(false);
    }
  }, [mappers?.response, firstTimeLoad]);

  useEffect(() => {
    return () => {
      queryClient.removeQueries({
        queryKey: [PRODUCT_CACHE_KEYS.get_component_list],
      });
    };
  }, []);

  const [tabActiveKey, setTabActiveKey] = useState("request");
  const [isFormTouched, setIsFormTouched] = useState(false);

  const items: TabsProps["items"] = [
    {
      key: "request",
      label: "Request mapping",
      children: <RequestMapping />,
    },
    {
      key: "response",
      label: "Response mapping",
      children: <ResponseMapping />,
      disabled: loadingMapper,
    },
  ];
  const handleSelectSonataProp = (selected: any) => {
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
    }
    setRightSideInfo(undefined);
    setRightSide(undefined);
    setIsFormTouched(true);
  };
  const handleSelectSellerProp = (selected: any) => {
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
    setRightSideInfo(undefined);
    setRightSide(undefined);
  };
  const handleRevert = () => {
    setRequestMapping(resetMapping() ?? []);
    setResponseMapping(mappers?.response);
    setIsFormTouched(false);
  };

  const validateData = () => {
    const requiredRequest = requestMapping.filter(
      (rm: IRequestMapping) => rm.requiredMapping
    );
    const isRequiredAllRequest = every(
      requiredRequest,
      (rm: IRequestMapping) =>
        !isEmpty(get(rm, "target")) && !isEmpty(get(rm, "targetLocation"))
    );
    const requiredResponse = responseMapping.filter(
      (rm: IRequestMapping) => rm.requiredMapping
    );
    const isRequiredAllResponse = every(
      requiredResponse,
      (rm: IRequestMapping) =>
        !isEmpty(get(rm, "source")) && !isEmpty(get(rm, "sourceLocation"))
    );
    if (!isRequiredAllRequest) {
      notification.error({
        message: "Please fill all required request mapping fields",
        description:
          "Fields required: " +
          requiredRequest?.map((r) => r.source)?.join(", "),
      });
      return true;
    }
    if (!isRequiredAllResponse) {
      notification.error({
        message: "Please fill all required response mapping fields",
        description:
          "Fields required: " +
          requiredResponse?.map((r) => r.target)?.join(", "),
      });
      return true;
    }
    return false;
  };
  const handleSave = async (isExit?: boolean) => {
    try {
      if (isExit) {
        const isErrorValidation = validateData();
        if (isErrorValidation) {
          return;
        }
      }
      const newData = chain(listMappingStateResponse)
        .groupBy("name")
        .map((items, name) => ({
          name,
          valueMapping: flatMap(items, (item) =>
            item?.to?.map?.((to) => ({ [to]: item.from }))
          ),
        }))
        .value();
      let newResponse = cloneDeep(responseMapping);
      if (!isEmpty(newData)) {
        for (const it of newData) {
          newResponse = newResponse.map((rm) => {
            if (rm.name === it.name) {
              rm.valueMapping = reduce(
                it.valueMapping,
                (acc, obj) => {
                  return { ...acc, ...obj };
                },
                {}
              );
            }
            return rm;
          });
        }
      }
      const data = cloneDeep(mapperResponse.data[0]);
      data.facets.endpoints[0] = {
        ...data.facets.endpoints[0],
        serverKey,
        method: sellerApi.method,
        path: sellerApi.url,
        mappers: {
          request: requestMapping?.map((rm) => ({
            ...rm,
            target: rm.target?.replace("path.", "")?.replace("query.", ""),
            source: rm.source?.replace("path.", "")?.replace("query.", ""),
          })),
          response: newResponse,
        },
      };
      const res = await updateTargetMapper({
        productId: currentProduct,
        componentId: data.metadata.id,
        data,
      } as any);
      notification.success({ message: res.message });
      if (isExit) {
        navigate(-1);
      }
      setStep(1);
      setActiveKey("1");
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

  useEffect(() => {
    return () => {
      reset();
    };
  }, [location]);

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
          <Flex className={styles.breadcrumb} justifyContent="space-between">
            <Flex className={styles.infoBox}>
              {queryData.mappingStatus === "incomplete" && (
                <Flex gap={8}>
                  <Tag bordered={false} color="error">
                    Incomplete
                  </Tag>
                  {toDateTime(queryData.updatedAt)}
                  <Tooltip title="Last update">
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
              <DeployStandardAPI metadataKey={componentKey} />
              <Button className={styles.revertButton} onClick={handleRevert}>
                <RollbackIcon />
              </Button>
              <Button
                data-testid="btn-save"
                type="primary"
                onClick={() => handleSave(true)}
                loading={isPending}
                disabled={!isFormTouched}
              >
                Save
              </Button>
            </Flex>
          </Flex>
          <HeaderMapping />
          <Tabs
            items={items}
            activeKey={tabActiveKey}
            onChange={(ak) => {
              if (ak === "response" && !isEmpty(sellerApi)) {
                setRightSide(EnumRightType.AddSellerResponse);
              }
              setTabActiveKey(ak);
            }}
          />
        </div>
        <div className={styles.right}>
          {rightSide === EnumRightType.AddSonataProp && (
            <RightAddSonataProp
              spec={jsonSpec}
              method={queryData?.method}
              onSelect={handleSelectSonataProp}
            />
          )}
          {rightSide === EnumRightType.SelectSellerAPI && (
            <SelectAPI save={() => handleSave(false)} />
          )}
          {rightSide === EnumRightType.AddSellerProp && (
            <RightAddSellerProp onSelect={handleSelectSellerProp} />
          )}
          {rightSide === EnumRightType.AddSellerResponse && (
            <SelectResponseProperty />
          )}
        </div>
      </Flex>
    </Flex>
  );
};

export default NewAPIMapping;
