import type { CSSProperties, HTMLAttributes, ReactNode } from 'react';

import type { Property } from 'csstype';

interface Props extends HTMLAttributes<HTMLDivElement> {
  children?: ReactNode;
  justifyContent?: Property.JustifyContent;
  alignItems?: Property.AlignItems;
  flexDirection?: Property.FlexDirection;
  flexWrap?: Property.FlexWrap;
  gap?: number;
  className?: string;
  style?: CSSProperties;
}

const Flex = ({
  children,
  justifyContent = 'center',
  alignItems = 'center',
  flexDirection = 'row',
  flexWrap,
  gap,
  className,
  style,
  ...props
}: Props) => {
  return (
    <div
      {...props}
      style={{ display: 'flex', justifyContent, alignItems, flexDirection, gap, flexWrap, ...style }}
      className={className}
    >
      {children}
    </div>
  );
};

export default Flex;
