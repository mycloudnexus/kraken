import { Steps as AntSteps, StepsProps } from "antd";
import classNames from "classnames";
import styles from "./index.module.scss";

export function Steps(props: Readonly<StepsProps>) {
  return (
    <AntSteps
      {...props}
      className={classNames(
        styles.customSteps,
        props.size === "small" && styles.small
      )}
    />
  );
}
