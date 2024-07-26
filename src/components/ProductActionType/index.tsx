import { Flex } from "antd";
import styles from "./index.module.scss";
import { useCallback } from "react";

type Props = {
  productType?: string;
  actionType?: string;
};

const ProductActionType = ({ productType, actionType }: Props) => {
  const renderTextType = useCallback((type: string) => {
    switch (type) {
      case "access_e_line":
        return "Access E-line";
      case "uni":
        return "UNI";
      default:
        return type;
    }
  }, []);
  return (
    <Flex gap={8} align="center">
      {productType ? (
        <div className={styles.tagInfo}>{renderTextType(productType)}</div>
      ) : null}
      {actionType ? (
        <div style={{ textTransform: "capitalize" }} className={styles.tagInfo}>
          {actionType}
        </div>
      ) : null}
    </Flex>
  );
};

export default ProductActionType;
