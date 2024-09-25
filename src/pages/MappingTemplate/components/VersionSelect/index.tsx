import Text from "@/components/Text";
import styles from "./index.module.scss";
import { Flex, Tag } from "antd";
import { IReleaseHistory } from "@/utils/types/product.type";
import clsx from "clsx";
import { isEmpty } from "lodash";
import dayjs from "dayjs";
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
      <Flex vertical style={{ marginTop: 10 }}>
        {data?.data?.map((d) => (
          <Flex
            key={d.templateUpgradeId}
            align="center"
            justify="space-between"
            className={clsx([
              styles.item,
              !isEmpty(selectedVersion) &&
                d.productVersion === selectedVersion &&
                styles.selected,
            ])}
            onClick={() => setSelectedVersion(d.productVersion)}
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
