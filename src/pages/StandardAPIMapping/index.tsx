import {
  useGetComponentDetail,
  useCreateNewVersion,
  useGetComponentDetailMapping,
  useGetProductComponents,
} from "@/hooks/product";
import { Button, Flex, notification, Spin } from "antd";
import { showModalConfirmCreateVersion } from "./components/ModalConfirmCreateVersion";
import styles from "./index.module.scss";
import { delay, get, isEmpty } from "lodash";
import { useParams } from "react-router";
import { useAppStore } from "@/stores/app.store";
import { SUCCESS_CODE } from "@/utils/constants/api";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import ComponentSelect from "./components/ComponentSelect";
import MappingDetailsList from "./components/MappingDetailsList";
import { useCallback, useEffect, useMemo, useRef } from "react";
import { IMapperDetails } from "@/utils/types/env.type";
import NewAPIMapping from "../NewAPIMapping";
import groupByPath from "@/utils/helpers/groupByPath";
import { useBoolean } from "usehooks-ts";

const StandardAPIMapping = () => {
  const { currentProduct } = useAppStore();
  const { componentId } = useParams();
  const { activePath, setActivePath, selectedKey, setSelectedKey } =
    useMappingUiStore();
  const newAPIMappingRef = useRef<any>(null);

  const { setQuery, reset } = useNewApiMappingStore();
  const { data: componentDetail, isLoading } = useGetComponentDetail(
    currentProduct,
    componentId ?? ""
  );
  const { data: detailDataMapping, refetch } = useGetComponentDetailMapping(
    currentProduct,
    componentId ?? ""
  );
  const { data: componentList } = useGetProductComponents(currentProduct, {
    kind: "kraken.component.api",
  });
  const { mutateAsync: createNewVersion } = useCreateNewVersion();
  const { value: isChangeMappingKey, setValue: setIsChangeMappingKey } =
    useBoolean(false);

  const handleCreateNewVersion = useCallback(async () => {
    try {
      const data = {
        componentKey: componentId,
        productId: currentProduct,
        componentId,
      };
      const result = await createNewVersion(data as any);
      if (+result.code !== SUCCESS_CODE) throw new Error(result.message);
      notification.success({ message: "Create new version success" });
    } catch (error) {
      notification.error({
        message: get(
          error,
          "reason",
          get(error, "message", "Error. Please try again")
        ),
      });
    }
  }, [componentId, currentProduct, createNewVersion]);

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

  const handleDisplay = (mapItem: IMapperDetails) => {
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
    <Spin spinning={isLoading}>
      <Flex align="stretch" vertical className={styles.pageWrapper}>
        <Flex align="center" justify="space-between" style={{ padding: "5px 0" }}>
          <ComponentSelect
            componentList={componentList}
            componentName={componentName}
          />
          <Flex justify="end">
            <Button
              data-testid="btn-create-version"
              type="primary"
              onClick={() => {
                showModalConfirmCreateVersion({
                  className: styles.modalCreate,
                  onOk: handleCreateNewVersion,
                });
              }}
            >
              Create new version
            </Button>
          </Flex>
        </Flex>
        <Flex className={styles.pageBody}>
          <Flex vertical gap={12}>
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
          </Flex>
          <Flex vertical gap={20} className={styles.mainWrapper}>
            <Flex
              align="center"
              justify="center"
              className={styles.versionListWrapper}
            >
              {activePath && !isChangeMappingKey && (
                <NewAPIMapping
                  ref={newAPIMappingRef}
                  refetch={refetch}
                  isRequiredMapping={isRequiredMapping}
                />
              )}
            </Flex>
          </Flex>
        </Flex>
      </Flex>
    </Spin>
  );
};

export default StandardAPIMapping;
