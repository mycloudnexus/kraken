import { AlertProps, Alert as AntAlert } from "antd";
import classNames from "classnames";
import styles from "./index.module.scss";

export function Alert({ className, ...props }: Readonly<AlertProps>) {
  return (
    <AntAlert
      {...props}
      showIcon
      className={classNames(className, styles.customAlert)}
    />
  );
}
