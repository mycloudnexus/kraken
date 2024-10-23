import DoneIcon from "@/assets/icon/upgrade-done.svg";
import ReleaseBg from "@/assets/release-bg.svg?url";
import { Text } from "@/components/Text";
import { useGetMappingTemplateReleaseHistoryList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { useMappingTemplateStore } from "@/stores/mappingTemplate";
import { IReleaseHistory } from "@/utils/types/product.type";
import { RightOutlined } from "@ant-design/icons";
import { Empty, Flex, Spin, Tag } from "antd";
import { isEmpty } from "lodash";
import { useState, useMemo } from "react";
import VersionDetail from "../VersionDetail";
import VersionSelect from "../VersionSelect";
import styles from "./index.module.scss";

const ReleaseHistory = () => {
  const { currentProduct } = useAppStore();
  const { releaseParams } = useMappingTemplateStore();
  const { data: releaseData, isLoading } =
    useGetMappingTemplateReleaseHistoryList(currentProduct, releaseParams);
  const [selectedVersion, setSelectedVersion] = useState("");

  const currentData = useMemo(() => {
    if (isEmpty(releaseData)) return [];
    setSelectedVersion(releaseData?.data?.[0]?.templateUpgradeId);
    return releaseData;
  }, [releaseData]);

  const currentDetailInfo = useMemo(() => {
    return releaseData?.data?.find(
      (d: IReleaseHistory) => d.templateUpgradeId === selectedVersion
    );
  }, [releaseData, selectedVersion]);
  return (
    <div className={styles.root}>
      <Flex
        vertical
        className={styles.info}
        gap={16}
        style={{ backgroundImage: `url(${ReleaseBg})` }}
      >
        <Text.Custom size="24px" lineHeight="32px" bold="500" color="#fff">
          What’s new of each release
        </Text.Custom>
        <Text.LightMedium color="#fff" lineHeight="22px">
          Process to upgrade Kraken to new version
        </Text.LightMedium>
        <Flex align="center" gap={8}>
          <Tag bordered={false} className={styles.basicTag}>
            Deploy to Stage
          </Tag>
          <RightOutlined style={{ color: "#fff", fontSize: 8 }} />
          <Tag bordered={false} className={styles.basicTag}>
            Test
          </Tag>
          <RightOutlined style={{ color: "#fff", fontSize: 8 }} />
          <Tag bordered={false} className={styles.basicTag}>
            Deploy to Production
          </Tag>
          <RightOutlined style={{ color: "#fff", fontSize: 8 }} />
          <Tag bordered={false} className={styles.specialTag}>
            <DoneIcon />
            Upgrade done!
          </Tag>
        </Flex>
      </Flex>
      <Flex className={styles.content}>
        {!isLoading && isEmpty(currentData?.data) && (
          <Empty description="No release history" className={styles.empty} />
        )}
        {isLoading ? (
          <Spin style={{ width: "100%" }} />
        ) : (
          <>
            <VersionSelect
              data={currentData}
              selectedVersion={selectedVersion}
              setSelectedVersion={setSelectedVersion}
            />
            <VersionDetail data={currentDetailInfo} />
          </>
        )}
      </Flex>
    </div>
  );
};

export default ReleaseHistory;
