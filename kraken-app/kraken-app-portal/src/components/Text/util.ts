export type FontWeight = 'bold' | 'medium' | 'light'
export type FontSize = 'large' | 'normal' | 'small' | 'tiny'

export function getFontWeight(weight: FontWeight) {
  switch (weight) {
    case 'bold': return 700
    case 'light': return 400
    case 'medium': return 500
    default: return 500
  }
}

export function getFontSize(size: FontSize) {
  switch (size) {
    case 'large': return 16
    case 'normal': return 14
    case 'small': return 12
    case 'tiny': return 10
    default: return 14
  }
}
