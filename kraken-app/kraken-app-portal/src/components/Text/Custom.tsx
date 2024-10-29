import { HTMLAttributes, ReactNode, CSSProperties } from "react";

export interface TextProps extends HTMLAttributes<HTMLElement> {
  children?: ReactNode;
  size?: string | number;
  bold?: string | number;
  color?: string;
  style?: CSSProperties;
  className?: string;
  lineHeight?: string;
  fontStyle?: "normal" | "italic" | "oblique" | "initial" | "inherit";
}

export const Custom = ({
  children,
  size = "14px",
  bold = "500",
  color = "var(--gray-10)",
  style,
  className,
  lineHeight,
  fontStyle = "normal",
  ...props
}: TextProps) => {
  return (
    <span
      {...props}
      style={{
        fontSize: size,
        fontWeight: bold,
        color,
        lineHeight,
        fontStyle,
        ...style,
      }}
      className={className}
    >
      {children}
    </span>
  );
};