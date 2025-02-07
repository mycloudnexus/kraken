import UpgradingIcon from "@/assets/icon/upgrading.svg";
import Logo from "@/assets/logo.svg";
import { useUser } from "@/hooks/user/useUser";
import { useTutorialStore } from "@/stores/tutorial.store";
import { ISystemInfo } from "@/utils/types/user.type";
import {
  EditTwoTone,
  LogoutOutlined,
  QuestionCircleOutlined,
} from "@ant-design/icons";
import { Button, Card, Divider, Dropdown, Flex, Tag } from "antd";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Text } from "../Text";
import { UserAvatar } from "./UserAvatar";
import styles from "./index.module.scss";

const Header = ({ info }: Readonly<{ info?: ISystemInfo }>) => {
  const location = useLocation();
  const { currentUser } = useUser();
  const { toggleTutorial } = useTutorialStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.clear();
    navigate("/login");
  };

  const dropdownRender = () => {
    return (
      <Card
        className={styles.root}
        actions={[
          <Flex
            data-testid="logoutOpt"
            key="log-out"
            align="center"
            gap={10}
            justify="center"
            onClick={handleLogout}
          >
            <LogoutOutlined style={{ color: "#FF3864" }} />
            <Text.LightMedium color="#FF3864">Log Out</Text.LightMedium>
          </Flex>,
        ]}
      >
        <Flex justify="center" align="center" vertical gap={8}>
          <div style={{ position: "relative" }}>
            <UserAvatar
              size={64}
              className={styles.avatarLg}
              user={currentUser}
            />
            <div className={styles.edit}>
              <EditTwoTone style={{ fontSize: 12 }} />
            </div>
          </div>
          <Text.Custom size="20px" lineHeight="28px" bold="500">
            {currentUser?.name}
          </Text.Custom>
          <Text.LightMedium lineHeight="22px" color="rgba(0, 0, 0, 0.45)">
            {currentUser?.email}
          </Text.LightMedium>
        </Flex>
      </Card>
    );
  };

  return (
    <div className={styles.header}>
      <Flex gap={16} align="center">
        <Link data-testid="logo" to="/" className={styles.logo}>
          <Logo />
          MEF LSO API Adaptor
        </Link>

        <Divider type="vertical" className={styles.divider} />

        <Text.LightMedium data-testid="productName">
          {info?.productName || info?.productKey}
        </Text.LightMedium>
        <Tag
          data-testid="productSpec"
          style={{
            background: "var(--bg)",
            color: "var(--text-secondary)",
            border: "none",
          }}
        >
          {info?.productSpec}
        </Tag>
      </Flex>

      <div className={styles.rightMenu}>
        {/* This mean the template mapping is in Upgrading process */}
        {info?.status && info.status !== "RUNNING" && (
          <>
            <Link
              data-testid="mappingInProgress"
              to="/mapping-template"
              className={styles.mappingInProgress}
            >
              <UpgradingIcon />
              Mapping template upgrading
            </Link>
            <Divider type="vertical" style={{ height: 16, padding: 0 }} />
          </>
        )}

        {/^\/api-mapping/.test(location.pathname) && (
          <Button
            type="link"
            style={{ color: "var(--text)", padding: "0 4px" }}
            onClick={toggleTutorial}
          >
            <QuestionCircleOutlined />
          </Button>
        )}

        <Dropdown
          placement="bottomRight"
          className={styles.avatar}
          menu={{ items: [] }}
          dropdownRender={dropdownRender}
        >
          <UserAvatar size={24} user={currentUser} />
        </Dropdown>
      </div>
    </div>
  );
};

export default Header;
