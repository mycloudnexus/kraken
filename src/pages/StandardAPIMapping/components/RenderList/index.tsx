import LogMethodTag from "@/components/LogMethodTag";
import Text from "@/components/Text";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { ROUTES } from "@/utils/constants/route";
import { Button, Flex, Tag, Typography } from "antd";
import { capitalize } from "lodash";
import { useCallback } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./index.module.scss";

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
              <LogMethodTag method={s.method} />
              <Text.NormalMedium>{s.path}</Text.NormalMedium>
              <Text.NormalMedium color="rgba(0,0,0,0.45)"> </Text.NormalMedium>
              <Button type="link" style={{ paddingInline: 4 }}>
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
