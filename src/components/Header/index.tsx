import {
  Avatar,
  Button,
  Card,
  Dropdown,
  Flex,
  Input,
  Select,
  Tooltip,
} from "antd";
import styles from "./index.module.scss";
import Logo from "@/assets/logo.svg";
import {
  BellOutlined,
  CloseOutlined,
  DownOutlined,
  EditTwoTone,
  LogoutOutlined,
  QuestionCircleOutlined,
  SearchOutlined,
  UpOutlined,
  UserOutlined,
} from "@ant-design/icons";
import { DEFAULT_PRODUCT } from "@/utils/constants/product";
import { useAppStore } from "@/stores/app.store";
import { useTutorialStore } from "@/stores/tutorial.store";
import useUser from "@/hooks/user/useUser";
import Text from "../Text";
import { useNavigate } from "react-router-dom";

const TooltipBody = (setTutorialCompleted: (value: boolean) => void) => (
  <div className={styles.tooltip}>
    <Flex justify="space-between">
      <span>Open the guide here.</span>
      <CloseOutlined
        style={{ fontSize: 12 }}
        onClick={() => setTutorialCompleted(false)}
      />
    </Flex>
    <Flex justify="end">
      <Button
        onClick={() => {
          setTutorialCompleted(false);
        }}
      >
        Got it
      </Button>
    </Flex>
  </div>
);

const Header = () => {
  const { currentUser } = useUser();
  const { currentProduct } = useAppStore();
  const { tutorialCompleted, setTutorialCompleted, setOpenTutorial } =
    useTutorialStore();
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
            <Avatar size={64}>
              <UserOutlined />
            </Avatar>
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
      <Flex gap={48}>
        <div className={styles.logo}>
          <Logo />
          KRAKEN
        </div>
        <Select
          allowClear={false}
          value={currentProduct}
          className={styles.select}
          options={[
            {
              value: DEFAULT_PRODUCT,
              label: <span style={{ fontWeight: 500 }}>MEF Sonata API</span>,
            },
          ]}
          suffixIcon={
            <div className={styles.selectIcon}>
              <UpOutlined style={{ fontSize: 8 }} />
              <DownOutlined style={{ fontSize: 8 }} />
            </div>
          }
        />
      </Flex>

      <div className={styles.rightMenu}>
        <Input
          placeholder="Search"
          suffix={<SearchOutlined style={{ cursor: "pointer" }} />}
          style={{ borderRadius: "156px" }}
        />
        <Tooltip
          align={{ offset: [12, 15] }}
          overlayInnerStyle={{ zIndex: 1 }}
          placement="bottomLeft"
          open={tutorialCompleted || undefined}
          title={TooltipBody(setTutorialCompleted)}
          rootClassName={styles.tooltipBlue}
        >
          <QuestionCircleOutlined onClick={() => setOpenTutorial(true)} />
        </Tooltip>

        <BellOutlined />
        <Dropdown
          placement="bottomRight"
          className={styles.avatar}
          menu={{ items: [] }}
          dropdownRender={dropdownRender}
        >
          <UserOutlined />
        </Dropdown>
      </div>
    </div>
  );
};

export default Header;
