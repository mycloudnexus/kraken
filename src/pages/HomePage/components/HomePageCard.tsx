import Text from "@/components/Text";
import { useManualGetComponentList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { API_SERVER_KEY } from "@/utils/constants/product";
import { ROUTES } from "@/utils/constants/route";
import { Button, Typography } from "antd";
import { isEmpty } from "lodash";
import { useNavigate } from "react-router-dom";
import { useBoolean } from "usehooks-ts";
import styles from "./index.module.scss";

type Props = {
  description: string;
  title: string;
  icon: any;
  version: string;
};

const { Paragraph } = Typography;

const HomePageCard = ({
  description = "",
  title = "",
  icon,
  version = "",
}: Props) => {
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();
  const {
    value: isHover,
    setTrue: trueHover,
    setFalse: falseHover,
  } = useBoolean(false);
  const { mutateAsync: runGet } = useManualGetComponentList();

  const load = async () => {
    const dataList = await runGet({
      productId: currentProduct,
      params: {
        kind: API_SERVER_KEY,
      },
    } as any);
    if (isEmpty(dataList?.data?.data)) {
      navigate(`/component/${currentProduct}/new`);
      return;
    }

    navigate(`/component/${currentProduct}/list`);
  };
  const toEnvOverview = () => {
    navigate(ROUTES.ENV_OVERVIEW);
  };
  return (
    <div
      className={styles.card}
      onMouseEnter={trueHover}
      onMouseLeave={falseHover}
    >
      {icon}
      <p>
        <Text.NormalMedium>
          {title}
          {version ? (
            <span className={styles.titleIcon}>
              <Text.LightTiny color="#fff">{version}</Text.LightTiny>
            </span>
          ) : null}
        </Text.NormalMedium>
      </p>
      <Paragraph
        ellipsis={{ rows: 3, expandable: false }}
        style={{ fontSize: 13, color: "#717788", fontWeight: 500 }}
      >
        {description}
      </Paragraph>
      {isHover && (
        <span className={styles.hoverContent}>
          <div className={styles.hoverWrapper}>
            <Button
              shape="round"
              type="primary"
              className={styles.btn}
              onClick={load}
            >
              Seller API Set up
            </Button>
            <Button shape="round" type="primary" className={styles.btn}>
              Standard API Mapping
            </Button>
            <Button
              shape="round"
              type="primary"
              className={styles.btn}
              onClick={toEnvOverview}
            >
              Environment Overview
            </Button>
          </div>
        </span>
      )}
    </div>
  );
};

export default HomePageCard;
