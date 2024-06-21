import { Tag } from "antd";
import { useMemo } from "react";
import styles from "./index.module.scss";

const availableMethods = ["POST", "GET", "PUT", "PATCH", "DELETE"];

interface Props {
  method: string;
}
const LogMethodTag = ({ method }: Readonly<Props>) => {
  const uppercasedMethod = method?.toLocaleUpperCase();
  const correctMethod = availableMethods.includes(uppercasedMethod);
  const color = useMemo(() => {
    if (!correctMethod) return "default";
    return {
      GET: "green",
      POST: "blue",
      PATCH: "cyan",
      PUT: "orange",
      DELETE: "red",
    }[uppercasedMethod];
  }, [uppercasedMethod, correctMethod]);

  if (!correctMethod) return null;

  return (
    <Tag bordered={false} color={color} className={styles.tag}>
      {uppercasedMethod}
    </Tag>
  );
};

export default LogMethodTag;
