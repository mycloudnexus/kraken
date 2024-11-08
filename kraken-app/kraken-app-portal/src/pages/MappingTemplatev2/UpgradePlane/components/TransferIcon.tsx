import PolygonIcon from "@/assets/icon/polygon.svg";
import classNames from "classnames";
import styles from "../index.module.scss";

export function TransferIcon({
  active,
  completed,
}: Readonly<{ active?: boolean; completed?: boolean }>) {
  return (
    <span
      className={classNames(styles.upgradeIcon, {
        [styles.upgraded]: completed,
        [styles.upgrading]: active && !completed,
      })}
    >
      <PolygonIcon />
      <PolygonIcon />
      <PolygonIcon />
    </span>
  );
}
