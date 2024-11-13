import classNames from "classnames";
import styles from './index.module.scss'
import { useEffect, useState } from "react";
import { InputProps } from "antd";

export function AutoGrowingInput({
  prefix, suffix, className, value, placeholder, onChange, onBlur, ...rest
}: Readonly<{ value?: string; onChange?: (value: string) => void } & Omit<InputProps, 'value' | 'onChange'>>) {
  const [text, setText] = useState<string>('')
  const [isFocused, setIsFocused] = useState(false)

  useEffect(() => {
    setText(value ?? '')
  }, [value, setText])

  return (
    <div {...rest}
      className={classNames(className, styles.autoGrowingInput, isFocused && styles.focused)}
    >
      {prefix}
      <span
        className={styles.content}
        tabIndex={0}
        role="textbox"
        contentEditable='true'
        dangerouslySetInnerHTML={{ __html: text }}
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        style={{ '--placeholder': `"${placeholder}"` }}
        onFocus={() => setIsFocused(true)}
        onBlur={e => {
          setIsFocused(false)
          onChange?.(e.target.textContent ?? '')
          onBlur?.(e as any)
        }}
        onKeyDown={e => {
          if (e.key === 'Enter') {
            e.preventDefault()
          }
        }} />
      {suffix}
    </div>
  )
}