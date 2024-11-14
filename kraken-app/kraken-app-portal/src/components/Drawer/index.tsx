import { CloseOutlined } from "@ant-design/icons";
import { Drawer as AntDrawer, DrawerProps } from "antd";
import classNames from "classnames";
import { PropsWithChildren } from "react";
import styles from "./index.module.scss";

export function Drawer({
  title,
  className,
  onClose,
  children,
  ...props
}: Readonly<PropsWithChildren<DrawerProps>>) {
  return (
    <AntDrawer
      {...props}
      title={
        <>
          {title}
          <CloseOutlined onClick={onClose} />
        </>
      }
      className={classNames(className, styles.customDrawer)}
    >
      {children}
    </AntDrawer>
  );
}
