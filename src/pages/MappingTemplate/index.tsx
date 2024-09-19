import Text from "@/components/Text";
import styles from "./index.module.scss";
import { Flex, Input, Tabs } from "antd";
import { useState } from "react";
import CurrentVersion from "./components/CurrentVersion";
import ReleaseHistory from "./components/ReleaseHistory";
import UpgradeHistory from "./components/UpgradeHistory";
const MappingTemplate = () => {
  const [activeKey, setActiveKey] = useState("1");

  return (
    <div className={styles.root}>
      <Text.LightLarge>Mapping template release & Upgrade</Text.LightLarge>
      <div className={styles.container}>
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
        {activeKey === "1" ? <ReleaseHistory /> : <UpgradeHistory />}
      </div>
    </div>
  );
};

export default MappingTemplate;
