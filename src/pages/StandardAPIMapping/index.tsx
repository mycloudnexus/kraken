import Text from "@/components/Text";
import {
  useGetComponentDetail,
  useCreateNewVersion,
  useGetComponentDetailMapping,
  useGetProductComponents,
} from "@/hooks/product";
import { DoubleLeftOutlined } from "@ant-design/icons";
import {
  Button,
  Divider,
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
import ComponentSelect from './components/ComponentSelect';
import MappingDetailsList from './components/MappingDetailsList';
import { useCallback, useMemo } from 'react';
import { IMapperDetails } from '@/utils/types/env.type';
import NewAPIMapping from '../NewAPIMapping';

const StandardAPIMapping = () => {
  const { currentProduct } = useAppStore();
  const { componentId } = useParams();
  const { setQuery, activePath, setActivePath, reset } = useNewApiMappingStore();
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

  const handleDisplay = useCallback((mapItem: IMapperDetails) => {
    reset();
    setActivePath(mapItem.path);
    setQuery(JSON.stringify(mapItem));
  }, [reset, setActivePath, setQuery]);

  const componentName = useMemo(() => get(componentDetail, "metadata.name", ""), [componentDetail]);

  return (
    <Flex align="stretch" className={styles.pageWrapper}>
      <Flex vertical gap={12}>
        <ComponentSelect componentList={componentList} componentName={componentName} />
        <Flex vertical justify="space-between" className={styles.leftWrapper}>
          <Spin spinning={isLoading}>
            <MappingDetailsList detailDataMapping={detailDataMapping} setActiveSelected={handleDisplay} />
          </Spin>
          <Flex vertical align="center" gap={8} className={styles.leftBottomWrapper}>
            <Text.NormalSmall color="#bfbfbf">Version 1.0</Text.NormalSmall>
            <Divider style={{ margin: 0 }} />
            <Button className={styles.switcherBtn}>
              <DoubleLeftOutlined />
            </Button>
          </Flex>
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
          {activePath && <NewAPIMapping />}
        </div>
      </Flex>
    </Flex>
  );
};

export default StandardAPIMapping;
