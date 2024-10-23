import { Input as AntInput, InputProps } from "antd";
import { useEffect, useState } from "react";

/**
 * Blur to trigger onChange, in order to boost performance
 * @param props antd InputProps
 * @returns Input
 */
export function Input({
  value,
  onChange,
  ...props
}: Readonly<
  Omit<InputProps, "onChange"> & { onChange?: (value: string) => void }
>) {
  const [inputValue, setInputValue] = useState("");

  useEffect(() => {
    setInputValue(value as string);
  }, [value, setInputValue]);

  return (
    <AntInput
      {...props}
      value={inputValue}
      onChange={(e) => setInputValue(e.target.value)}
      onBlur={() => onChange?.(inputValue)}
    />
  );
}
