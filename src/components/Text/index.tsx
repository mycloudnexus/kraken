import type { CSSProperties, HTMLAttributes, ReactNode } from "react";
interface Props extends HTMLAttributes<HTMLElement> {
  children?: ReactNode;
  size?: string;
  bold?: string;
  color?: string;
  style?: CSSProperties;
  className?: string;
  lineHeight?: string;
  fontStyle?: "normal" | "italic" | "oblique" | "initial" | "inherit";
}

const Custom = ({
  children,
  size = "14px",
  bold = "500",
  color = "var(--gray-10)",
  style,
  className,
  lineHeight,
  fontStyle = "normal",
  ...props
}: Props) => {
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

const LightSmall = ({ children, color, style, className, ...props }: Props) => {
  return (
    <Custom
      {...props}
      size="12px"
      bold="400"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

const NormalSmall = ({
  children,
  color,
  style,
  className,
  ...props
}: Props) => {
  return (
    <Custom
      {...props}
      size="12px"
      bold="500"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

const BoldSmall = ({ children, color, style, className, ...props }: Props) => {
  return (
    <Custom
      {...props}
      size="12px"
      bold="700"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

const LightMedium = ({
  children,
  color,
  style,
  className,
  ...props
}: Props) => {
  return (
    <Custom
      {...props}
      size="14px"
      bold="400"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

const NormalMedium = ({
  children,
  color,
  style,
  className,
  ...props
}: Props) => {
  return (
    <Custom
      {...props}
      size="14px"
      bold="500"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

const BoldMedium = ({ children, color, style, className, ...props }: Props) => {
  return (
    <Custom
      {...props}
      size="14px"
      bold="700"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

const LightLarge = ({ children, color, style, className, ...props }: Props) => {
  return (
    <Custom
      {...props}
      size="16px"
      bold="400"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

const NormalLarge = ({
  children,
  color,
  style,
  className,
  ...props
}: Props) => {
  return (
    <Custom
      {...props}
      size="16px"
      bold="500"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

const BoldLarge = ({ children, color, style, className, ...props }: Props) => {
  return (
    <Custom
      {...props}
      size="16px"
      bold="700"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

const LightTiny = ({ children, color, style, className, ...props }: Props) => {
  return (
    <Custom
      {...props}
      size="10px"
      bold="400"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

const NormalTiny = ({ children, color, style, className, ...props }: Props) => {
  return (
    <Custom
      {...props}
      size="10px"
      bold="500"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

const BoldTiny = ({ children, color, style, className, ...props }: Props) => {
  return (
    <Custom
      {...props}
      size="10px"
      bold="700"
      color={color}
      style={style}
      className={className}
    >
      {children}
    </Custom>
  );
};

export const Text = {
  Custom,
  LightSmall,
  NormalSmall,
  BoldSmall,
  LightMedium,
  NormalMedium,
  BoldMedium,
  LightLarge,
  NormalLarge,
  BoldLarge,
  LightTiny,
  NormalTiny,
  BoldTiny,
};

export default Text;
