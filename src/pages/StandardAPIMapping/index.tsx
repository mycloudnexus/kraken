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
  Tabs,
} from "antd";

import { useEffect, useMemo, useState } from "react";
import { showModalConfirmCreateVersion } from "./components/ModalConfirmCreateVersion";
import RenderList, { IMapProductAndType } from "./components/RenderList";
import styles from "./index.module.scss";
import { get, uniq } from "lodash";
import { useParams } from "react-router";
import { useAppStore } from "@/stores/app.store";
import { SUCCESS_CODE } from "@/utils/constants/api";
import MappingDetailsList from '@/components/MappingDetailsList';
import ComponentSelect from '@/components/ComponentSelect';

const StandardAPIMapping = () => {
  const { currentProduct } = useAppStore();
  const { componentId } = useParams();
  // hooks
  const { data, isLoading } = useGetComponentDetail(
    currentProduct,
    componentId ?? ""
  );

  const { data: detailDataMapping } = useGetComponentDetailMapping(
    currentProduct,
    componentId ?? ""
  );
  const { mutateAsync: runCreateNewVersion } = useCreateNewVersion();

  const { data: componentList } = useGetProductComponents(
    currentProduct,
    {
      kind: "kraken.component.api",
    }
  );

  const { noTab, tabs } = useMemo(() => {
    if (isLoading) {
      return {
        noTab: true,
        tabs: [],
      };
    }
    const tabs: string[] = uniq(
      data?.facets?.supportedProductTypesAndActions?.reduce(
        (agg: string[], s: IMapProductAndType) => [
          ...agg,
          ...(s?.productTypes ?? []),
        ],
        []
      )
    );
    if (tabs.length === 0) {
      return {
        noTab: true,
        tabs: [],
      };
    }
    const tabWithInfo = tabs.map((tab: string) => ({
      name: tab,
      data: data?.facets?.supportedProductTypesAndActions?.filter(
        (s: IMapProductAndType) => s?.productTypes?.includes(tab)
      ),
    }));
    return {
      noTab: false,
      tabs: tabWithInfo,
    };
  }, [data, isLoading]);

  const handleCreateNewVersion = async () => {
    try {
      const data: any = {
        componentKey: componentId,
        productId: currentProduct,
        componentId,
      };
      const result = await runCreateNewVersion(data);
      if (+result.code !== SUCCESS_CODE) {
        throw new Error(result.message);
      }
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
  };

  const [tab, setTab] = useState("");

  useEffect(() => {
    if (!noTab) {
      setTab("");
    }
    if (tabs.length) {
      setTab(tabs[0].name);
    }
  }, [noTab, tabs, componentList]);



  const componentName = get(data, "metadata.name", "");

  return (
    <Flex align="stretch" className={styles.pageWrapper}>
        <Flex vertical gap={12}>
          <ComponentSelect componentList={componentList} componentName={componentName} />
          <Flex  vertical justify="space-between" className={styles.leftWrapper}>
            <Flex vertical gap={8}>
              <MappingDetailsList detailDataMapping={detailDataMapping} setActiveSelected={() => alert('Not implemented!')} />
            </Flex>
            <Flex
              vertical
              align="center"
              gap={8}
              className={styles.leftBottomWrapper}
            >
              <Text.NormalSmall color="#bfbfbf">Version 1.0</Text.NormalSmall>
              <Divider style={{ margin: 0 }} />
              <Button className={styles.switcherBtn}>
                <DoubleLeftOutlined />
              </Button>
            </Flex>
          </Flex>
        </Flex>

        <Flex vertical gap={20} className={styles.mainWrapper}>
          <Flex justify="end" >
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
            <Spin spinning={isLoading}>
              {noTab ? (
                <RenderList
                  data={data?.facets?.supportedProductTypesAndActions}
                  componentId={componentId}
                  tab={undefined}
                />
              ) : (
                <Tabs
                  items={tabs.map(({ name, data }) => ({
                    key: name,
                    label: name,
                    children: (
                      <RenderList
                        data={data}
                        componentId={componentId}
                        tab={tab}
                      />
                    ),
                  }))}
                  onChange={(key) => setTab(key)}
                />
              )}
            </Spin>
          </div>
        </Flex>

    </Flex>
  );
};

export default StandardAPIMapping;
