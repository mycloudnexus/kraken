import { Layout, Spin } from "antd";
import { Suspense } from "react";
import { Outlet } from "react-router-dom";
import Header from "../Header";
import SideNavigation from "../SideNavigation";
import styles from "./index.module.scss";

const BasicLayout = () => {
  return (
    <div className={styles.main}>
      <Header />
      <Layout hasSider className={styles.content}>
        <SideNavigation />
        <Suspense fallback={<Spin fullscreen />}>
          <Outlet />
        </Suspense>
      </Layout>
    </div>
  );
};

export default BasicLayout;
