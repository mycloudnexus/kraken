import { Input, Select } from "antd";
import styles from "./index.module.scss";
import Logo from "@/assets/logo.svg";
import {
  BellOutlined,
  DownOutlined,
  QuestionCircleOutlined,
  SearchOutlined,
  UpOutlined,
  UserOutlined,
} from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { useCallback } from "react";
import { DEFAULT_PRODUCT } from "@/utils/constants/product";
import { useAppStore } from "@/stores/app.store";

const Header = () => {
  const navigate = useNavigate();
  const { currentProduct } = useAppStore();
  const goHome = useCallback(() => {
    () => {
      navigate("/");
    };
  }, []);

  return (
    <div className={styles.header}>
      <div className={styles.logo} onClick={goHome}>
        <Logo />
        KRAKEN
      </div>
      <div className={styles.rightMenu}>
        <Select
          allowClear={false}
          value={currentProduct}
          className={styles.select}
          options={[
            {
              value: DEFAULT_PRODUCT,
              label: <span>MEF Sonata API</span>,
            },
          ]}
          suffixIcon={
            <div className={styles.selectIcon}>
              <UpOutlined style={{ fontSize: 8 }} />
              <DownOutlined style={{ fontSize: 8 }} />
            </div>
          }
        />
        <Input
          placeholder="Search"
          suffix={<SearchOutlined style={{ cursor: "pointer" }} />}
          style={{ borderRadius: "156px" }}
        />
        <QuestionCircleOutlined />
        <BellOutlined />
        <div className={styles.avatar}>
          <UserOutlined />
        </div>
      </div>
    </div>
  );
};

export default Header;
