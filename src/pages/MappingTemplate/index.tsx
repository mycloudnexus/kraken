import { Text } from "@/components/Text";
import styles from "./index.module.scss";
import { Flex, Input, Tabs } from "antd";
import { useRef, useState } from "react";
import CurrentVersion from "./components/CurrentVersion";
import ReleaseHistory from "./components/ReleaseHistory";
import UpgradeHistory from "./components/UpgradeHistory";
import useSize from "@/hooks/useSize";
import { get } from "lodash";
const MappingTemplate = () => {
  const [activeKey, setActiveKey] = useState("1");
  const ref = useRef<any>();
  const size = useSize(ref);

  return (
    <div className={styles.root} ref={ref}>
      <Text.LightLarge>Mapping template release & Upgrade</Text.LightLarge>
      <div
        className={styles.container}
        style={{ maxHeight: get(size, "height", 0) - 28 }}
      >
        <Flex justify="space-between" align="center">
          <Tabs
            activeKey={activeKey}
            onChange={(key) => setActiveKey(key)}
            items={[
              { label: "Release history", key: "1" },
              { label: "Upgrade history", key: "2" },
            ]}
          />
          {activeKey === "1" ? (
            <CurrentVersion />
          ) : (
            <Input.Search placeholder="Search" style={{ width: 264 }} />
          )}
        </Flex>
        {activeKey === "1" ? (
          <ReleaseHistory maxHeight={get(size, "height", 0) - 114} />
        ) : (
          <UpgradeHistory />
        )}
      </div>
    </div>
  );
};

export default MappingTemplate;
