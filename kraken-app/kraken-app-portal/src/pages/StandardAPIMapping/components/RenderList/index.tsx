import LogMethodTag from "@/components/LogMethodTag";
import { Text } from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { ROUTES } from "@/utils/constants/route";
import { Button, Flex, Tag, Typography } from "antd";
import { capitalize, get } from "lodash";
import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./index.module.scss";
import APIViewerModal from "@/components/APIViewerModal";
import { useBoolean } from "usehooks-ts";
import { useGetComponentDetail } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";

export interface IMapProductAndType {
  path: string;
  method: string;
  actionTypes?: string[];
  productTypes?: string[];
}

const buildQuery = (
  mapItem: IMapProductAndType,
  tab?: string,
  actionType?: string
) => {
  const query: Record<string, any> = {
    method: mapItem.method,
    path: mapItem.path,
  };
  if (tab) {
    query.productType = tab.toLocaleLowerCase();
  }
  if (actionType) {
    query.actionType = actionType;
  }
  return query;
};

interface Props {
  data?: IMapProductAndType[];
  componentId?: string;
  tab?: string;
}
const RenderList = ({ data, componentId, tab }: Readonly<Props>) => {
  const { currentProduct } = useAppStore();
  const {
    value: isOpenModal,
    setTrue: openModal,
    setFalse: closeModal,
  } = useBoolean(false);
  const { data: dataDetail } = useGetComponentDetail(
    currentProduct,
    (componentId ?? "").replace(".api.", ".api-spec."),
    isOpenModal
  );
  const [selectedAPI, setSelectedAPI] = useState("");
  const navigate = useNavigate();
  const { setQuery } = useNewApiMappingStore();

  const hasAction = useCallback(
    (s: IMapProductAndType) => (s.actionTypes?.length ?? 0) > 0,
    []
  );
  const gotoMapping =
    (mapItem: IMapProductAndType, tab?: string, actionType?: string) => () => {
      const query = buildQuery(mapItem, tab, actionType);
      setQuery(JSON.stringify(query));
      navigate(ROUTES.NEW_API_MAPPING(componentId!));
    };
  const handleViewAPI = async (mapItem: IMapProductAndType) => {
    const path = `/${get(mapItem, "path", "")?.split("/").slice(5).join("/")}`;
    setSelectedAPI(`${path} ${mapItem.method}`);
    openModal();
  };
  if (!data) return null;
  return (
    <Flex vertical gap={12}>
      {isOpenModal && (
        <APIViewerModal
          content={get(dataDetail, "facets.baseSpec.content", "")}
          isOpen={isOpenModal}
          onClose={closeModal}
          selectedAPI={selectedAPI}
        />
      )}
      {data.map((s: IMapProductAndType) => (
        <Flex
          vertical
          align="stretch"
          justify="space-between"
          gap={14}
          key={`${s.method} - ${s.path}`}
          className={styles.mappingWrapper}
        >
          <Flex align="center" justify="space-between">
            <Flex align="center" gap={16}>
              <LogMethodTag method={s.method} />
              <Text.NormalMedium>{s.path}</Text.NormalMedium>
              <Text.NormalMedium color="rgba(0,0,0,0.45)"> </Text.NormalMedium>
              <Button
                type="link"
                style={{ paddingInline: 4 }}
                onClick={() => handleViewAPI(s)}
              >
                View
              </Button>
            </Flex>
            {!hasAction(s) ? (
              <Button type="primary" onClick={gotoMapping(s, tab)}>
                Edit Mapping
              </Button>
            ) : null}
          </Flex>
          {hasAction(s) && (
            <>
              {s.actionTypes?.map((type) => (
                <Flex
                  vertical
                  gap={14}
                  className={styles.actionWrapper}
                  key={type}
                >
                  <Flex align="center" justify="space-between">
                    <Tag>{capitalize(type)}</Tag>
                    <Button type="primary" onClick={gotoMapping(s, tab, type)}>
                      Edit Mapping
                    </Button>
                  </Flex>
                </Flex>
              ))}
            </>
          )}
          <Flex className={styles.actionWrapper}>
            <Flex align="vertical" gap={8}>
              <Typography.Text style={{ color: "rgba(0, 0, 0, 0.45)" }}>
                Last modified at
              </Typography.Text>
              <Typography.Text style={{ color: "rgba(0, 0, 0, 0.45)" }}>
                2024-05-15 04:34:56
              </Typography.Text>
              <Typography.Text style={{ color: "rgba(0, 0, 0, 0.45)" }}>
                By User name
              </Typography.Text>
              <Tag bordered={false} color="error">
                Incomplete mapping
              </Tag>
            </Flex>
          </Flex>
        </Flex>
      ))}
    </Flex>
  );
};

export default RenderList;
