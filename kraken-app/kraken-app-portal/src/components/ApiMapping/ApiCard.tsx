import { IRunningMapping } from "@/utils/types/env.type";
import { Flex } from "antd";
import classNames from "classnames";
import { ReactNode } from "react";
import MappingMatrix from "../MappingMatrix";
import RequestMethod from "../Method";
import styles from "./index.module.scss";

export function ApiCard({
  apiInstance,
  prefix,
  suffix,
  className,
  onClick,
}: Readonly<{
  apiInstance: IRunningMapping;
  prefix?: ReactNode;
  suffix?: ReactNode;
  className?: string;
  mappingMatrixPosition?: "left" | "right";
  onClick?: () => void;
}>) {
  return (
    <Flex
      data-testid="useCase"
      align="center"
      gap={10}
      className={classNames(className, styles.rowBorder)}
      onClick={onClick}
    >
      {prefix}
      <RequestMethod method={apiInstance?.method} />
      <span data-testid="apiPath">
        {"/" + apiInstance.path.split("/").slice(-2).join("/")}
      </span>
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
