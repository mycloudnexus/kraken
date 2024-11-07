import { Layout, Spin } from "antd";
import { Suspense } from "react";
import { Outlet } from "react-router-dom";
import Header from "../Header";
import SideNavigation from "../SideNavigation";
import styles from "./index.module.scss";
import { useGetSystemInfo } from "@/hooks/user";
import { useLongPolling } from "@/hooks/useLongPolling";

const BasicLayout = () => {
  const { data: systemInfo } = useLongPolling(useGetSystemInfo(), 60 * 1000) // 1 min

  return (
    <div className={styles.main}>
      <Header info={systemInfo} />
      <Layout hasSider className={styles.content}>
        <SideNavigation info={systemInfo} />
        <Suspense fallback={<Spin fullscreen />}>
          <Outlet />
        </Suspense>
      </Layout>
    </div>
  );
};

export default BasicLayout;
