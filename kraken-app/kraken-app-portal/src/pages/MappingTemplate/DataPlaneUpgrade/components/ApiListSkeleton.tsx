import { Flex, Skeleton } from "antd";
import styles from "../index.module.scss";

const arr = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

export function ApiListSkeleton() {
  return (
    <>
      {arr.map((val) => (
        <Flex gap={10} key={val} className={styles.skeleton} align="center">
          <Skeleton.Input active style={{ width: 40, height: 20 }} />
          <Skeleton.Input active style={{ flex: 1 }} />

          <Flex gap={10} style={{ marginLeft: "auto" }}>
            <Skeleton.Input active style={{ width: 100, height: 32 }} />
            <Skeleton.Input active style={{ width: 100, height: 32 }} />
          </Flex>
        </Flex>
      ))}
    </>
  );
}
