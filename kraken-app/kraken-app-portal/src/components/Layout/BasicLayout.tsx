import { Outlet } from "react-router-dom";
import Header from "../Header";
import styles from "./index.module.scss";
import SideNavigation from '../SideNavigation';
import { Layout } from 'antd';

const BasicLayout = () => {
  return (
    <div className={styles.main}>
      <Header />
      <Layout hasSider className={styles.content}>
        <SideNavigation />
        <Outlet />
      </Layout>
    </div>
  );
};

export default BasicLayout;
