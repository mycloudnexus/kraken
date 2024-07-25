import {
  useGetComponentDetail,
  useCreateNewVersion,
  useGetComponentDetailMapping,
  useGetProductComponents,
} from "@/hooks/product";
import {
  Button,
  Flex,
  notification,
  Spin,
} from "antd";
import { showModalConfirmCreateVersion } from "./components/ModalConfirmCreateVersion";
import styles from "./index.module.scss";
import { get } from "lodash";
import { useParams } from "react-router";
import { useAppStore } from "@/stores/app.store";
import { SUCCESS_CODE } from "@/utils/constants/api";
import { useNewApiMappingStore } from '@/stores/newApiMapping.store';
import { useMappingUiStore } from '@/stores/mappingUi.store';
import ComponentSelect from './components/ComponentSelect';
import MappingDetailsList from './components/MappingDetailsList';
import { useCallback, useMemo, useRef } from 'react';
import { IMapperDetails } from '@/utils/types/env.type';
import NewAPIMapping from '../NewAPIMapping';
import groupByPath from '@/utils/helpers/groupByPath';
import { showModalChangePath } from '../NewAPIMapping/components/ModalChangePath';

const StandardAPIMapping = () => {
  const { currentProduct } = useAppStore();
  const { componentId } = useParams();
  const { activePath, mappingInProgress, setActivePath, setMappingInProgress } = useMappingUiStore();
  const newAPIMappingRef = useRef<any>(null);

  const { setQuery, reset } = useNewApiMappingStore();
  const { data: componentDetail, isLoading } = useGetComponentDetail(currentProduct, componentId ?? "");
  const { data: detailDataMapping } = useGetComponentDetailMapping(currentProduct, componentId ?? "");
  const { data: componentList } = useGetProductComponents(currentProduct, { kind: "kraken.component.api" });
  const { mutateAsync: createNewVersion } = useCreateNewVersion();

  const handleCreateNewVersion = useCallback(async () => {
    try {
      const data = { componentKey: componentId, productId: currentProduct, componentId };
      const result = await createNewVersion(data as any);
      if (+result.code !== SUCCESS_CODE) throw new Error(result.message);
      notification.success({ message: "Create new version success" });
    } catch (error) {
      notification.error({
        message: get(error, "reason", get(error, "message", "Error. Please try again")),
      });
    }
  }, [componentId, currentProduct, createNewVersion]);

  const resetState = (mapItem: IMapperDetails) => {
    reset();
    setActivePath(mapItem.path);
    setQuery(JSON.stringify(mapItem));
    setMappingInProgress(false);
  }

  const handleDisplay = (mapItem: IMapperDetails) => {
    if (mappingInProgress) {
      showModalChangePath(
        () => {
          newAPIMappingRef.current.handleSave(true)
          resetState(mapItem)
        },
        () => resetState(mapItem)
      )
    } else {
      resetState(mapItem);
    }
  };

  const componentName = useMemo(() => get(componentDetail, "metadata.name", ""), [componentDetail]);
  const { groupedPaths, isGroupedPathsEmpty } = useMemo(() => {
    const groupedPaths = groupByPath(get(detailDataMapping, "details", []))
    const isGroupedPathsEmpty = Object.keys(groupedPaths).length === 0;
    return { groupedPaths, isGroupedPathsEmpty }
  }, [detailDataMapping]);


  return (
    <Flex align="stretch" className={styles.pageWrapper}>
      <Flex vertical gap={12}>
        <ComponentSelect componentList={componentList} componentName={componentName} />
        <Flex vertical justify="space-between" className={styles.leftWrapper}>
          <Spin spinning={isLoading}>
            {!isGroupedPathsEmpty && <MappingDetailsList groupedPaths={groupedPaths} setActiveSelected={handleDisplay} />}
          </Spin>
        </Flex>
      </Flex>
      <Flex vertical gap={20} className={styles.mainWrapper}>
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
        <div className={styles.versionListWrapper}>
          {activePath && <NewAPIMapping ref={newAPIMappingRef} />}
        </div>
      </Flex>
    </Flex>
  );
};

export default StandardAPIMapping;
