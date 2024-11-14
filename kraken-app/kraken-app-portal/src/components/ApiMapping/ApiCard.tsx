import { IRunningMapping } from "@/utils/types/env.type";
import { Flex, Tooltip } from "antd";
import classNames from "classnames";
import { ReactNode } from "react";
import MappingMatrix from "../MappingMatrix";
import RequestMethod from "../Method";
import styles from "./index.module.scss";
import { trimPath } from "@/utils/helpers/url";

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
      <Tooltip title={apiInstance.path}>
        <span data-testid="apiPath">
          {".../" + trimPath(apiInstance.path, 2)}
        </span>
      </Tooltip>
      <Flex
        gap={8}
        align="center"
        flex={1}
      >
        <MappingMatrix
          mappingMatrix={apiInstance?.mappingMatrix}
          extraKey={"item.path"}
          isItemActive={false}
        />
      </Flex>
      {suffix}
    </Flex>
  );
}
