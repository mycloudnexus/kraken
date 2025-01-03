import classNames from "classnames";
import styles from './index.module.scss'
import { useEffect, useState } from "react";
import { InputProps } from "antd";

export function AutoGrowingInput({
  prefix, suffix, className, value, placeholder, disabled, onChange, onBlur, ...rest
}: Readonly<{ value?: string; onChange?: (value: string) => void } & Omit<InputProps, 'value' | 'onChange'>>) {
  const [text, setText] = useState<string>('')
  const [isFocused, setIsFocused] = useState(false)

  useEffect(() => {
    setText(value ?? '')
  }, [value, setText])

  return (
    <div {...rest}
      className={classNames(className, styles.autoGrowingInput, isFocused && styles.focused, disabled && styles.disabled)}
    >
      {prefix}
      <span
        className={classNames(styles.content, disabled && styles.disabled)}
        tabIndex={0}
        role="textbox"
        contentEditable={disabled ? 'false' : 'true'}
        dangerouslySetInnerHTML={{ __html: text }}
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        style={{ '--placeholder': `"${placeholder}"` }}
        onFocus={() => {
          if (!disabled) {
            setIsFocused(true)
          }
        }}
        onBlur={e => {
          setIsFocused(false)
          if (!disabled) {
            onChange?.(e.target.textContent ?? '')
            onBlur?.(e as any)
          }
        }}
        onKeyDown={e => {
          if (e.key === 'Enter' || disabled) {
            e.preventDefault()
          }
        }} />
      {suffix}
    </div>
  )
}