import { Flex } from "antd";
import classNames from "classnames";
import styles from "./index.module.scss";

export function PageTitle({
  children,
  className,
  ...props
}: Readonly<React.PropsWithChildren<React.HTMLAttributes<HTMLElement>>>) {
  return (
    <Flex
      {...props}
      data-testid="pageTitle"
      className={classNames(className, styles.pageTitle)}
      justify="space-between"
      align="center"
    >
      {children}
    </Flex>
  );
}
