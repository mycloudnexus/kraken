import { Tooltip } from "antd";
import classNames from "classnames";
import { HTMLAttributes, ReactNode, CSSProperties } from "react";
import styles from "./index.module.scss";

export interface TextProps extends HTMLAttributes<HTMLElement> {
  children?: ReactNode;
  size?: string | number;
  bold?: string | number;
  color?: string;
  style?: CSSProperties;
  className?: string;
  lineHeight?: string;
  fontStyle?: "normal" | "italic" | "oblique" | "initial" | "inherit";
  ellipsis?: boolean;
}

export const Custom = ({
  children,
  size = "14px",
  bold = "500",
  color = "var(--gray-10)",
  style,
  className,
  fontStyle = "normal",
  ellipsis,
  ...props
}: TextProps) => {
  return (
    <Tooltip title={ellipsis && children}>
      <span
        {...props}
        style={{
          ...style,
          fontSize: size,
          fontWeight: bold,
          color,
          fontStyle,
        }}
        className={classNames(className, {
          [styles.ellipsis]: ellipsis,
        })}
      >
        {children}
      </span>
    </Tooltip>
  );
};
