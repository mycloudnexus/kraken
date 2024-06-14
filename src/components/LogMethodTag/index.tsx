import { Tag } from "antd";
import { useMemo } from "react";
import styles from "./index.module.scss";

const availableMethods = ["POST", "GET", "PUT", "PATCH", "DELETE"];

interface Props {
  method: string;
}
const LogMethodTag = ({ method }: Readonly<Props>) => {
  const correctMethod = availableMethods.includes(method);
  const color = useMemo(() => {
    if (!correctMethod) return "default";
    return {
      GET: "green",
      POST: "blue",
      PATCH: "cyan",
      PUT: "orange",
      DELETE: "red",
    }[method];
  }, [method]);

  if (!correctMethod) return null;

  return (
    <Tag bordered={false} color={color} className={styles.tag}>
      {method}
    </Tag>
  );
};

export default LogMethodTag;
