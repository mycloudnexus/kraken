import { Flex, Skeleton } from "antd";
import classNames from "classnames";
import styles from "./index.module.scss";

const arr = [1, 2, 3, 4, 5, 6, 7];

export function ListVersionSkeleton() {
  return (
    <>
      {arr.map((val) => (
        <Flex
          key={val}
          className={classNames(styles.item, styles.skeleton)}
          gap={8}
          vertical
        >
          <Flex gap={6}>
            <Skeleton.Input active style={{ width: 40 }} />
            <Skeleton.Input active style={{ width: 40 }} />
            <Skeleton.Input active className={styles.fixedRight} />
          </Flex>

          <Skeleton.Input active />
        </Flex>
      ))}
    </>
  );
}
