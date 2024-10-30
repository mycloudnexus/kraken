import classNames from "classnames";
import { PageTitle } from "./PageTitle";
import styles from "./index.module.scss";

export function PageLayout({
  title,
  className,
  children,
  ...props
}: Readonly<
  React.PropsWithChildren<
    { title: React.ReactNode } & Omit<
      React.HTMLAttributes<HTMLDivElement>,
      "title"
    >
  >
>) {
  return (
    <main {...props} className={classNames(className, styles.layout)}>
      <PageTitle>{title}</PageTitle>

      <section className={styles.pageContent}>{children}</section>
    </main>
  );
}
