import { PageLayout } from "@/components/Layout";
import { Flex, Input, Tabs } from "antd";
import { useRef, useState } from "react";
import CurrentVersion from "./components/CurrentVersion";
import ReleaseHistory from "./components/ReleaseHistory";
import UpgradeHistory from "./components/UpgradeHistory";
import styles from "./index.module.scss";

const MappingTemplate = () => {
  const [activeKey, setActiveKey] = useState("1");
  const ref = useRef<any>();

  return (
    <PageLayout title="Mapping template release & Upgrade">
      <div ref={ref} className={styles.container}>
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
    </PageLayout>
  );
};

export default MappingTemplate;
