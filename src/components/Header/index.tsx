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

const Header = () => {
  const navigate = useNavigate();
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
          value="MEF LSO Sonata API Adaptors"
          className={styles.select}
          options={[
            {
              value: "MEF LSO Sonata API Adaptors",
              label: <span>MEF LSO Sonata API Adaptors</span>,
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
          suffix={<SearchOutlined style={{ cursor: 'pointer' }} />}
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
