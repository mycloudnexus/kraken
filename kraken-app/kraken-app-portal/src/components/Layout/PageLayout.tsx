import classNames from "classnames";
import { PageTitle } from "./PageTitle";
import styles from "./index.module.scss";

export function PageLayout({
  title,
  className,
  children,
  flex,
  /**
   * required: `flex` to be set
   */
  vertical,
  ...props
}: Readonly<
  React.PropsWithChildren<
    { title: React.ReactNode; flex?: boolean; vertical?: boolean } & Omit<
      React.HTMLAttributes<HTMLDivElement>,
      "title"
    >
  >
>) {
  return (
    <main {...props} className={classNames(className, styles.layout)}>
      <PageTitle>{title}</PageTitle>

      <section
        className={classNames(styles.pageContent, {
          [styles.flex]: flex,
          [styles.vertical]: vertical,
        })}
      >
        {children}
      </section>
    </main>
  );
}
