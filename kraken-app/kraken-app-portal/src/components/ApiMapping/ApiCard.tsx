import { IRunningMapping } from "@/utils/types/env.type";
import { Flex } from "antd";
import classNames from "classnames";
import { ReactNode } from "react";
import MappingMatrix from "../MappingMatrix";
import RequestMethod from "../Method";
import styles from "./index.module.scss";
import TrimmedPath from "../TrimmedPath";

export function ApiCard({
  apiInstance,
  prefix,
  suffix,
  className,
  style,
  onClick,
}: Readonly<{
  apiInstance: IRunningMapping;
  prefix?: ReactNode;
  suffix?: ReactNode;
  className?: string;
  style?: React.CSSProperties
  mappingMatrixPosition?: "left" | "right";
  onClick?: () => void;
}>) {
  return (
    <Flex
      data-testid="useCase"
      align="center"
      gap={10}
      style={style}
      className={classNames(className, styles.rowBorder)}
      onClick={onClick}
    >
      {prefix}
      <RequestMethod method={apiInstance?.method} />

      <TrimmedPath path={apiInstance.path} />

      <MappingMatrix
        align='center'
        flex={1}
        mappingMatrix={apiInstance?.mappingMatrix}
        extraKey={"item.path"}
        isItemActive={false}
      />

      {suffix}
    </Flex>
  );
}
