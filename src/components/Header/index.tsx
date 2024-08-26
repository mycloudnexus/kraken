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
import { DEFAULT_PRODUCT } from "@/utils/constants/product";
import { useAppStore } from "@/stores/app.store";
import { useTutorialStore } from "@/stores/tutorial.store";

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
  const { currentProduct } = useAppStore();
  const { tutorialCompleted, setTutorialCompleted, setOpenTutorial } =
    useTutorialStore();

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
        <div className={styles.avatar}>
          <UserOutlined />
        </div>
      </div>
    </div>
  );
};

export default Header;
