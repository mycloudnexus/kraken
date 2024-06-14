import LogMethodTag from "@/components/LogMethodTag";
import Text from "@/components/Text";
import { Button, Collapse, Flex } from "antd";
import { useCallback } from "react";
import styles from "./index.module.scss";

export interface IMapProductAndType {
  path: string;
  method: string;
  actionTypes?: string[];
  productTypes?: string[];
}

const RenderList = ({ data }: { data?: IMapProductAndType[] }) => {
  const hasAction = useCallback(
    (s: IMapProductAndType) => (s.actionTypes?.length ?? 0) > 0,
    []
  );
  if (!data) return null
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
              <Text.NormalMedium color="rgba(0,0,0,0.45)">
                Description??
              </Text.NormalMedium>
            </Flex>
            <Flex align="center" gap={8}>
              <Button style={{ marginRight: hasAction(s) ? 96 : 0 }}>
                View
              </Button>
              {!hasAction(s) ? <Button type="primary">Mapping</Button> : null}
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
                        Description??
                      </Text.NormalMedium>
                    </Flex>
                    <Button type="primary">Mapping</Button>
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

export default RenderList