import RollbackIcon from "@/assets/newAPIMapping/Rollback.svg";
import BreadCrumb from "@/components/Breadcrumb";
import DeployStage from "@/components/DeployStage";
import { PageLayout } from "@/components/Layout";
import {
  useGetComponentDetail,
  useGetComponentDetailMapping,
  useUpdateTargetMapper,
  useGetLatestRunningList,
} from "@/hooks/product";
import useSize from "@/hooks/useSize";
import { useAppStore } from "@/stores/app.store";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import buildInitListMapping from "@/utils/helpers/buildInitListMapping";
import groupByPath from "@/utils/helpers/groupByPath";
import { IMappers } from "@/utils/types/component.type";
import { IMapperDetails } from "@/utils/types/env.type";
import { Flex, Spin, Button, Tooltip, notification, Drawer } from "antd";
import dayjs from "dayjs";
import { delay, get, isEmpty, chain, cloneDeep, flatMap, reduce } from "lodash";
import { useEffect, useMemo, useRef, useState } from "react";
import { useParams } from "react-router";
import { useBoolean } from "usehooks-ts";
import { useLocation } from "react-router-dom";
import NewAPIMapping from "../NewAPIMapping";
import DeployHistory from "../NewAPIMapping/components/DeployHistory";
import { Deployment } from "../NewAPIMapping/components/Deployment";
import { IMapping } from "../NewAPIMapping/components/ResponseMapping";
import useGetApiSpec from "../NewAPIMapping/components/useGetApiSpec";
import useGetDefaultSellerApi from "../NewAPIMapping/components/useGetDefaultSellerApi";
import ComponentSelect from "./components/ComponentSelect";
import MappingDetailsList from "./components/MappingDetailsList";
import styles from "./index.module.scss";

const StandardAPIMapping = () => {
  const { currentProduct } = useAppStore();
  const { componentId } = useParams();
  const location = useLocation();
  const [mainTitle, setMainTitle] = useState(() => location?.state?.mainTitle ?? "unknown main title");
  const [filteredComponentList, setFilteredComponentList] = useState(() => location?.state?.filteredComponentList ?? []);
  const [productType, setProductType] = useState(() => location?.state?.productType ?? "");
  const { activePath, setActivePath, selectedKey, setSelectedKey } =
    useMappingUiStore();

  const {
    setQuery,
    reset,
    query,
    sellerApi,
    serverKey,
    requestMapping,
    responseMapping,
    setRequestMapping,
    setResponseMapping,
    setSellerApi,
    setListMappingStateResponse,
    listMappingStateResponse,
    listMappingStateRequest,
  } = useNewApiMappingStore();
  const queryData = useMemo(() => JSON.parse(query ?? "{}"), [query]);
  const { data: componentDetail, isLoading } = useGetComponentDetail(
    currentProduct,
    componentId ?? ""
  );
  const { data: detailDataMapping, refetch } = useGetComponentDetailMapping(
    currentProduct,
    componentId ?? ""
  );
  
  const { value: isChangeMappingKey, setValue: setIsChangeMappingKey } =
    useBoolean(false);

  const { data: runningDeploymentData, isFetching: isFetchingDeploymentData } =
    useGetLatestRunningList(currentProduct, queryData?.targetMapperKey);

  const {
    serverKeyInfo,
    mappers,
    mapperResponse,
    metadataKey,
    resetMapping,
    refreshMappingDetail,
  } = useGetApiSpec(currentProduct, queryData.targetMapperKey ?? "");

  const { sellerApi: defaultSellerApi } = useGetDefaultSellerApi(
    currentProduct,
    serverKeyInfo as any
  );

  const { mutateAsync: updateTargetMapper, isPending } =
    useUpdateTargetMapper();

  const [open, setOpen] = useState(false);
  const ref = useRef<any>();
  const size = useSize(ref);

  const resetState = (mapItem: IMapperDetails) => {
    reset();
    setActivePath(mapItem.path);
    setQuery(JSON.stringify(mapItem));
  };

  useEffect(() => {
    // Silence unused setter warnings without changing state
    setMainTitle((prev: string) => prev);
    setFilteredComponentList((prev: any[]) => prev);
    setProductType((prev: string) => prev);
  }, []);

  useEffect(() => {
    const mapItem = detailDataMapping?.details.find(
      (item) => item.targetKey === selectedKey
    );
    if (mapItem) {
      setQuery(JSON.stringify(mapItem));
    }
  }, [detailDataMapping, selectedKey]);

  const handleDisplay = async (mapItem: IMapperDetails) => {
    setIsChangeMappingKey(true);
    resetState(mapItem);
    setSelectedKey(mapItem.targetKey);
    delay(() => setIsChangeMappingKey(false), 100);
  };

  const isRequiredMapping = useMemo(() => {
    if (activePath) {
      const currentItem = detailDataMapping?.details?.find(
        (i) => i.path === activePath
      );
      if (isEmpty(currentItem)) {
        return true;
      }
      return currentItem.requiredMapping;
    }
    return true;
  }, [activePath]);

  const componentName = useMemo(
    () => get(componentDetail, "metadata.name", ""),
    [componentDetail]
  );
  const { groupedPaths, isGroupedPathsEmpty } = useMemo(() => {
    const groupedPaths = groupByPath(get(detailDataMapping, "details", []));
    const isGroupedPathsEmpty = Object.keys(groupedPaths).length === 0;
    return { groupedPaths, isGroupedPathsEmpty };
  }, [detailDataMapping]);

  const leftPanelRef = useRef<HTMLDivElement>(null);
  const bar = useRef<HTMLDivElement>(null);
  const [isMouseDown, setIsMouseDown] = useState(false);
  const clientX = useRef(0);

  const { left = 0 } = leftPanelRef.current?.getBoundingClientRect() ?? {};

  const handleMouseMove: React.MouseEventHandler<HTMLDivElement> = (e) => {
    e.stopPropagation();
    e.preventDefault();

    if (isMouseDown) {
      clientX.current = e.clientX;
      if (bar.current) {
        bar.current.style.left = e.clientX - left + "px";
      }
    }
  };

  const handleMouseUp = () => {
    if (isMouseDown && leftPanelRef.current) {
      leftPanelRef.current.style.width = (clientX.current ?? 0) - left + "px";
    }
    setIsMouseDown(false);
  };

  const handleRevert = () => {
    setRequestMapping(resetMapping() ?? []);
    setResponseMapping(mappers?.response);
    setListMappingStateResponse(
      buildInitListMapping(mappers?.response as any, "response")
    );
    // Store to default seller api?
    setSellerApi(defaultSellerApi);
  };

  const requestValueMapping = (item: IMapping) => {
    return [{ [item.from as string]: item.to?.[0] }];
  };

  const responseValueMapping = (item: IMapping) => {
    return item?.to?.map((to) => ({ [to]: item.from }));
  };

  const getValueMapping = (item: IMapping, type: "request" | "response") => {
    return type === "request"
      ? requestValueMapping(item)
      : responseValueMapping(item);
  };

  const transformListMappingItem = (
    item: IMapping[],
    type: "request" | "response"
  ) => {
    return chain(item)
      .groupBy("name")
      .map((items, name) => ({
        name,
        valueMapping: flatMap(items, (item) => getValueMapping(item, type)),
      }))
      .value();
  };

  const getNewResponse = (
    newResponse: typeof responseMapping,
    it: {
      name: string;
      valueMapping: (
        | {
            [x: string]: string | undefined;
          }
        | undefined
      )[];
    }
  ) => {
    return newResponse.map((rm) => {
      if (rm.name === it.name) {
        rm.valueMapping = reduce(
          it.valueMapping,
          (acc, obj) => ({ ...acc, ...obj }),
          {}
        );
      }
      return rm;
    });
  };

  const handleSave = async (callback?: () => void) => {
    try {
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
          newResponse = getNewResponse(newResponse, it);
        });
      }
      let newRequest = cloneDeep(requestMapping);
      if (!isEmpty(newDataRequest)) {
        newDataRequest.forEach((it) => {
          newResponse = getNewResponse(newResponse, it);
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

  return (
    <PageLayout
      title={
        <Flex
          align="center"
          justify="space-between"
          style={{ padding: "5px 0" }}
        >
          <BreadCrumb
            mainTitle= {mainTitle}
            mainUrl="/components"
            items={[
              {
                title: (
                  <ComponentSelect
                    componentList={{data : filteredComponentList}}
                    componentName={componentName}
                    productType={productType}
                    middle={true}
                  />
                ),
                url: "",
              },
            ]}
            lastItem="Mapping details"
          />
        </Flex>
      }
    >
      <Spin spinning={isLoading}>
        <Flex
          data-testid="leftPanel"
          className={styles.pageBody}
          onMouseMove={handleMouseMove}
          onBlur={handleMouseUp}
          onMouseUp={handleMouseUp}
          vertical={true}
        >
          <Flex className={styles.leftWrapper} ref={leftPanelRef}>
            <Flex vertical style={{ width: "100%" }} gap={20}>
              <Flex className={styles.breadcrumb} justify="space-between">
                {!isGroupedPathsEmpty && (
                  <MappingDetailsList
                    groupedPaths={groupedPaths}
                    setActiveSelected={handleDisplay}
                  />
                )}
                <Flex
                  justify="flex-end"
                  gap={8}
                  style={{ paddingRight: "10px" }}
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
                    </>
                  )}
                  <DeployStage
                    inComplete={queryData.mappingStatus === "incomplete"}
                    diffWithStage={queryData.diffWithStage}
                    metadataKey={metadataKey as any}
                  />
                </Flex>
              </Flex>
              <Flex className={styles.infoBox} justify="space-between">
                {queryData?.lastDeployedAt && (
                  <Deployment
                    deploymentData={runningDeploymentData}
                    loading={isFetchingDeploymentData}
                  />
                )}
                <Button type="link" onClick={() => setOpen(true)}>
                  Deploy History
                </Button>
              </Flex>
            </Flex>
          </Flex>
          {!isGroupedPathsEmpty && (
            <Flex
              align="center"
              justify="center"
              className={styles.versionListWrapper}
            >
              {activePath && !isChangeMappingKey && (
                <NewAPIMapping isRequiredMapping={isRequiredMapping} />
              )}
            </Flex>
          )}
        </Flex>
        <Drawer
          title="Deploy History"
          open={open}
          destroyOnClose={true}
          onClose={() => setOpen(false)}
          width={1200}
        >
          <DeployHistory
            targetMapperKey={queryData.targetMapperKey}
            scrollHeight={get(size, "height", 0) + 70}
          />
        </Drawer>
      </Spin>
    </PageLayout>
  );
};

export default StandardAPIMapping;
