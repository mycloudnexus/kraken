import { Button, Flex, Input, Select, Tooltip } from "antd";
import styles from "./index.module.scss";
import Logo from "@/assets/logo.svg";
import {
  BellOutlined,
  CloseOutlined,
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
import { useTutorialStore } from '@/stores/tutorial.store';

const TooltipBody = (setTutorialCompleted: (value: boolean) => void) => (
  <div className={styles.tooltip}>
    <Flex justify='space-between'>
      <span>Open the guide here.</span>
      <CloseOutlined
        style={{ fontSize: 12 }}
        onClick={() => setTutorialCompleted(false)}
      />
    </Flex>
    <Flex justify='end'>
      <Button onClick={() => {
        setTutorialCompleted(false)
      }}>Got it</Button>
    </Flex>
  </div>
);

const Header = () => {
  const navigate = useNavigate();
  const { currentProduct } = useAppStore();
  const goHome = useCallback(() => {
    navigate("/");
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  const { tutorialCompleted, setTutorialCompleted, setOpenTutorial } = useTutorialStore();

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
        <Tooltip
          align={{ offset: [12, 15] }}
          overlayInnerStyle={{ zIndex: 1 }}
          placement='bottomLeft'
          open={tutorialCompleted || undefined}
          title={TooltipBody(setTutorialCompleted)}
          rootClassName={styles.tooltipBlue}
        >
          <QuestionCircleOutlined onClick={() => setOpenTutorial(true)} />
        </Tooltip>

        <BellOutlined />
        <div className={styles.avatar}>
          <UserOutlined />
        </div>
      </div>
    </div >
  );
};

export default Header;
