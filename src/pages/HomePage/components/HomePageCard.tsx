import Text from "@/components/Text";
import styles from "./index.module.scss";
import { useBoolean } from "usehooks-ts";
import { Button, Typography } from "antd";
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
  const {
    value: isHover,
    setTrue: trueHover,
    setFalse: falseHover,
  } = useBoolean(false);
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
            <Button shape="round" type="primary" className={styles.btn}>
              Seller API Set up
            </Button>
            <Button shape="round" type="primary" className={styles.btn}>
              Standard API Mapping
            </Button>
            <Button shape="round" type="primary" className={styles.btn}>
              Environment Overview
            </Button>
          </div>
        </span>
      )}
    </div>
  );
};

export default HomePageCard;
