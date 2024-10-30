import { useGetProductEnvs } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { Flex, Tabs } from "antd";
import { Text } from "../Text";

import { useEffect, useMemo } from "react";
import { capitalize, get, isEmpty } from "lodash";
import styles from "./index.module.scss";

type Props = {
  value: any;
  onChange: (value: any) => void;
};

const EnvSelect = ({ value, onChange }: Props) => {
  const { currentProduct } = useAppStore();
  const { data } = useGetProductEnvs(currentProduct);

  const stageId = useMemo(() => {
    const stage = data?.data?.find(
      (env: any) => env.name?.toLowerCase() === "stage"
    );
    return stage?.id;
  }, [data?.data]);

  useEffect(() => {
    if (!isEmpty(value)) {
      return;
    }
    onChange(stageId);
  }, [stageId, value]);

  const tabItems = useMemo(() => {
    const sortItems = get(data, "data", [])
      .sort((a, b) => {
        if (a.name === "stage") return -1; // stage đứng đầu
        if (a.name === "production") return 1;
        if (b.name === "stage") return 1;
        if (b.name === "production") return -1;
        return 0;
      })
      .map((env) => ({
        key: env.id,
        label: (
          <Text.LightMedium>
            {capitalize(env.name?.toLowerCase())} environment
          </Text.LightMedium>
        ),
      }));
    return sortItems;
  }, [data?.data]);

  return (
    <Flex gap={6} align="center">
      <Tabs
        activeKey={value}
        items={tabItems}
        className={styles.tabs}
        onChange={(activeKey) => onChange(activeKey)}
      />
    </Flex>
  );
};

export default EnvSelect;
