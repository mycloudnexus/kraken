import { Outlet } from "react-router-dom";
import Header from "../Header";
import styles from "./index.module.scss";

const BasicLayout = () => {
  return (
    <div className={styles.main}>
      <Header />
      <div className={styles.content}>
        <Outlet />
      </div>
    </div>
  );
};

export default BasicLayout;
