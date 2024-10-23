import { Text } from "@/components/Text";
import { IReleaseHistory } from "@/utils/types/product.type";
import { Flex, Tag } from "antd";
import clsx from "clsx";
import dayjs from "dayjs";
import { isEmpty } from "lodash";
import styles from "./index.module.scss";

type Props = {
  data: {
    data: IReleaseHistory[];
    page: number;
    size: number;
    total: number;
  };
  selectedVersion: string;
  setSelectedVersion: (version: string) => void;
};
const VersionSelect = ({
  data,
  selectedVersion,
  setSelectedVersion,
}: Props) => {
  return (
    <div className={styles.root}>
      <Text.NormalMedium lineHeight="22px">Previous releases</Text.NormalMedium>
      <Flex vertical className={styles.versionList}>
        {data?.data?.map((d) => (
          <Flex
            key={d.templateUpgradeId}
            align="center"
            justify="space-between"
            className={clsx([
              styles.item,
              !isEmpty(selectedVersion) &&
                d.templateUpgradeId === selectedVersion &&
                styles.selected,
            ])}
            onClick={() => setSelectedVersion(d.templateUpgradeId)}
            role="none"
          >
            <Flex align="center" gap={4}>
              <Text.LightMedium>{d.productVersion}</Text.LightMedium>
              <Tag color="#F0F2F5" className={styles.prodTag}>
                {d.productSpec}
              </Tag>
            </Flex>
            <Text.LightSmall color="#00000073">
              {dayjs(d.publishDate).format("YYYY-MM-DD")}
            </Text.LightSmall>
          </Flex>
        ))}
      </Flex>
    </div>
  );
};

export default VersionSelect;
