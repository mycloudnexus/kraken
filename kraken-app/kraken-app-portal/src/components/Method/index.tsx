import { Tag } from "antd";
import { useMemo } from "react";
import styles from "./index.module.scss";
import classNames from "classnames";

const RequestMethod = ({ method = "", noSpace = false, disabled = false }) => {
  const methodColor = useMemo(() => {
    switch (method.toLowerCase()) {
      case "get":
        return "green";
      case "post":
        return "blue";
      case "put":
        return "orange";
      case "delete":
        return "red";
      default:
        return "gray";
    }
  }, [method]);

  return (
    <Tag
      data-testid="method"
      className={classNames(styles.requestMethod, disabled && styles.disabled)}
      bordered={false}
      color={methodColor}
      style={noSpace ? { marginRight: 0 } : {}}
    >
      {method.toUpperCase()}
    </Tag>
  );
};

export default RequestMethod;
