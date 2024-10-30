import { PropsWithChildren } from "react"
import { Custom, TextProps } from "./Custom"
import { FontSize, FontWeight, getFontSize, getFontWeight } from "./util"


const secondaryText = (weight: FontWeight) =>
  (size: FontSize) =>
    ({ children, ...props }: Readonly<PropsWithChildren<TextProps>>) =>
      <Custom
        {...props}
        size={getFontSize(size)}
        bold={getFontWeight(weight)}
        color="#00000073"
      >
        {children}
      </Custom>

export const SecondaryText = {
  Large: secondaryText('medium')('large'),
  Normal: secondaryText('medium')('normal'),
  Small: secondaryText('medium')('small'),
  BoldLarge: secondaryText('bold')('large'),
  BoldNormal: secondaryText('bold')('normal'),
  BoldSmall: secondaryText('bold')('small'),
  LightLarge: secondaryText('light')('large'),
  LightNormal: secondaryText('light')('normal'),
  LightSmall: secondaryText('light')('small'),
}