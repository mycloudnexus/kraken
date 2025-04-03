import { PageLayout } from "@/components/Layout";
import { Flex, Tabs } from "antd";
import { lazy, Suspense, useEffect, useRef, useState } from "react";
import styles from "./index.module.scss";
import config from "@/config.json";

const EnvironmentActivityLog = () => {
  const [mainTabKey, setMainTabKey] = useState<string>("activityLog");

  const [components, setComponents] = useState<any[]> ([]);

  const refWrapper = useRef<any>();

  useEffect(() => {
    async function loadTabs() {
      const componentPromises = config.activityLogPages.map(item => {
        const children = lazy(() => import(item.path));
        return {
          label: `${item.label}`,
          key: `${item.key}`,
          children: children,
        };
      });

      Promise.all(componentPromises).then(setComponents);
    }

    loadTabs();
  }, [config.activityLogPages]);

  return (
    <PageLayout
      title=""
    >
      <Flex
          align="center"
          justify="space-between"
          vertical={false}
          style={{ width: "100%" }}
        >
          <Suspense fallback={<div>Loading...Please Wait..</div>}>
            <Tabs activeKey={mainTabKey}
                        hideAdd
                        onChange={setMainTabKey}>
              {components.map((tab) => (
                <Tabs.TabPane tab={tab.label} key={tab.key}>
                  <div className={styles.contentWrapper} ref={refWrapper}>
                  <div className={styles.tableWrapper}>

                  <tab.children />

                    </div>
                    </div>
                </Tabs.TabPane>
              ))}
            </Tabs>
          </Suspense>
        </Flex>
    </PageLayout>
  );
};

export default EnvironmentActivityLog;
