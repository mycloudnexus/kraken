import { IReleaseHistory } from "@/utils/types/product.type";
import styles from "./index.module.scss";
import { Flex, Input } from "antd";
import Text from "@/components/Text";
import { isEmpty } from "lodash";
import VersionBtn from "../VersionBtn";

type Props = {
  data: IReleaseHistory;
};

const VersionDetail = ({ data }: Props) => {
  return (
    <div className={styles.root}>
      {isEmpty(data) ? (
        <></>
      ) : (
        <>
          <Flex justify="space-between" align="center">
            <Flex align="center" gap={16}>
              <Text.Custom size="20px" bold="500" lineHeight="28px">
                {data?.productVersion}
              </Text.Custom>
              <Text.LightMedium lineHeight="22px" color="#00000073">
                Released on {data?.publishDate}
              </Text.LightMedium>
            </Flex>
            <VersionBtn item={data} />
          </Flex>
          <div className={styles.container}>
            <Flex justify="space-between" align="center">
              <Text.NormalLarge className={styles.title}>
                What’s new in this version
              </Text.NormalLarge>
              <Input.Search style={{ width: 264 }} placeholder="Search" />
            </Flex>
            <div
              className={styles.content}
              dangerouslySetInnerHTML={{ __html: data?.description }}
            />
          </div>
        </>
      )}
    </div>
  );
};

export default VersionDetail;
