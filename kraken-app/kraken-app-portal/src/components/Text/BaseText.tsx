import { PropsWithChildren } from "react";
import { Custom, TextProps } from "./Custom";
import { FontSize, FontWeight, getFontSize, getFontWeight } from "./util";

const baseText = (weight: FontWeight) =>
  (size: FontSize) =>
    ({ children, ...props }: Readonly<PropsWithChildren<TextProps>>) =>
      <Custom
        {...props}
        size={getFontSize(size)}
        bold={getFontWeight(weight)}
      >
        {children}
      </Custom>

export const Text = {
  Custom,
  LightSmall: baseText('light')('small'),
  NormalSmall: baseText('medium')('small'),
  BoldSmall: baseText('bold')('small'),
  LightMedium: baseText('light')('normal'),
  NormalMedium: baseText('medium')('normal'),
  BoldMedium: baseText('bold')('normal'),
  LightLarge: baseText('light')('large'),
  NormalLarge: baseText('medium')('large'),
  BoldLarge: baseText('bold')('large'),
  LightTiny: baseText('light')('tiny'),
  NormalTiny: baseText('medium')('tiny'),
  BoldTiny: baseText('bold')('tiny'),
};
