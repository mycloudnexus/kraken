import BreadCrumb from "@/components/Breadcrumb";
import { PageLayout } from "@/components/Layout";
import {
  useGetComponentDetail,
  useGetComponentDetailMapping,
  useGetComponentListAPI,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import groupByPath from "@/utils/helpers/groupByPath";
import { IMapperDetails } from "@/utils/types/env.type";
import { Flex, Spin } from "antd";
import { delay, get, isEmpty } from "lodash";
import { useEffect, useMemo } from "react";
import { useParams } from "react-router";
import { useBoolean } from "usehooks-ts";
import NewAPIMapping from "../NewAPIMapping";
import ComponentSelect from "./components/ComponentSelect";
import MappingDetailsList from "./components/MappingDetailsList";
import styles from "./index.module.scss";

const StandardAPIMapping = () => {
  const { currentProduct } = useAppStore();
  const { componentId } = useParams();
  const { activePath, setActivePath, selectedKey, setSelectedKey } =
    useMappingUiStore();

  const { setQuery, reset } = useNewApiMappingStore();
  const { data: componentDetail, isLoading } = useGetComponentDetail(
    currentProduct,
    componentId ?? ""
  );
  const { data: detailDataMapping, refetch } = useGetComponentDetailMapping(
    currentProduct,
    componentId ?? ""
  );
  const { data: componentList } = useGetComponentListAPI(currentProduct);
  const { value: isChangeMappingKey, setValue: setIsChangeMappingKey } =
    useBoolean(false);

  const resetState = (mapItem: IMapperDetails) => {
    reset();
    setActivePath(mapItem.path);
    setQuery(JSON.stringify(mapItem));
  };

  useEffect(() => {
    const mapItem = detailDataMapping?.details.find(
      (item) => item.targetKey === selectedKey
    );
    if (mapItem) {
      setQuery(JSON.stringify(mapItem));
    }
  }, [detailDataMapping]);

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

  return (
    <PageLayout
      title={
        <Flex
          align="center"
          justify="space-between"
          style={{ padding: "5px 0" }}
        >
          <BreadCrumb
            mainTitle="Standard API mapping"
            mainUrl="/components"
            lastItem={
              <ComponentSelect
                componentList={componentList}
                componentName={componentName}
              />
            }
          />
        </Flex>
      }
    >
      <Spin spinning={isLoading}>
        <Flex className={styles.pageBody}>
          <Flex
            vertical
            justify={isLoading ? "center" : "space-between"}
            className={styles.leftWrapper}
          >
            {!isGroupedPathsEmpty && (
              <MappingDetailsList
                groupedPaths={groupedPaths}
                setActiveSelected={handleDisplay}
              />
            )}
          </Flex>

          <Flex
            align="center"
            justify="center"
            className={styles.versionListWrapper}
          >
            {activePath && !isChangeMappingKey ? (
              <NewAPIMapping
                refetch={refetch}
                isRequiredMapping={isRequiredMapping}
              />
            ) : (
              <div className={styles.empty}>
                <Spin size="large" />
              </div>
            )}
          </Flex>
        </Flex>
      </Spin>
    </PageLayout>
  );
};

export default StandardAPIMapping;
