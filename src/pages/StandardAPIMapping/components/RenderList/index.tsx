import LogMethodTag from "@/components/LogMethodTag";
import Text from "@/components/Text";
import { Button, Collapse, Flex } from "antd";
import { useCallback } from "react";
import styles from "./index.module.scss";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "@/utils/constants/route";

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
  let query: Record<string, any> = {
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
  if (!data) return null;
  return (
    <Flex vertical gap={12}>
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
              <LogMethodTag method={s.method.toUpperCase()} />
              <Text.NormalMedium>{s.path}</Text.NormalMedium>
              <Text.NormalMedium color="rgba(0,0,0,0.45)"> </Text.NormalMedium>
            </Flex>
            <Flex align="center" gap={8}>
              <Button style={{ marginRight: hasAction(s) ? 96 : 0 }}>
                View
              </Button>
              {!hasAction(s) ? (
                <Button type="primary" onClick={gotoMapping(s, tab)}>
                  Mapping
                </Button>
              ) : null}
            </Flex>
          </Flex>
          {hasAction(s) && (
            <Collapse
              ghost
              items={s.actionTypes?.map((type) => ({
                key: type,
                label: (
                  <Flex align="center" justify="space-between">
                    <Flex align="center" gap={12}>
                      <Text.NormalLarge>{type}</Text.NormalLarge>
                      <Text.NormalMedium color="rgba(0, 0, 0, 0.45)">
                        {" "}
                      </Text.NormalMedium>
                    </Flex>
                    <Button type="primary" onClick={gotoMapping(s, tab, type)}>
                      Mapping
                    </Button>
                  </Flex>
                ),
              }))}
              className={styles.collapse}
            />
          )}
        </Flex>
      ))}
    </Flex>
  );
};

export default RenderList;
