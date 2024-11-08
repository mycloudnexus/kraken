import DoneIcon from "@/assets/icon/upgrade-done.svg";
import ReleaseBg from "@/assets/release-bg.svg?url";
import { Text } from "@/components/Text";
import { useInfiniteReleaseHistoryQuery } from "@/hooks/mappingTemplate";
import { useAppStore } from "@/stores/app.store";
import { IReleaseHistory } from "@/utils/types/product.type";
import { RightOutlined } from "@ant-design/icons";
import { Empty, Flex, Tag } from "antd";
import { omit } from "lodash";
import { useState, useMemo, useEffect, Fragment } from "react";
import VersionDetail from "../VersionDetail";
import VersionSelect from "../VersionSelect";
import styles from "./index.module.scss";

const steps = [
  "Control plane upgrade",
  "Stage data plane update",
  "Test offline",
  "Production data plane upgrade",
  "Upgrade done!",
];

export default function ReleaseHistory() {
  const { currentProduct } = useAppStore();
  const {
    data: releaseData,
    hasNextPage,
    isFetching,
    isFetchingNextPage,
    fetchNextPage,
  } = useInfiniteReleaseHistoryQuery(
    currentProduct,
    omit({
      orderBy: "createdAt",
      direction: "DESC",
      size: 20,
    })
  );

  const [selectedVersion, setSelectedVersion] = useState("");

  const releases = releaseData?.pages?.flatMap((page) => page.data.data);

  useEffect(() => {
    if (releases?.length && !selectedVersion) {
      // Select latest version as default
      setSelectedVersion(releases[0].templateUpgradeId);
    }
  }, [releases, selectedVersion, setSelectedVersion]);

  const currentDetailInfo = useMemo(() => {
    return releases?.find(
      (d: IReleaseHistory) => d.templateUpgradeId === selectedVersion
    );
  }, [releases, selectedVersion]);

  return (
    <div className={styles.root}>
      <Flex
        vertical
        className={styles.info}
        justify="center"
        gap={16}
        style={{ backgroundImage: `url(${ReleaseBg})` }}
      >
        <Text.Custom
          data-testid="heading"
          size="24px"
          lineHeight="32px"
          bold="500"
          color="#fff"
        >
          What’s new of each release
        </Text.Custom>
        <Text.LightMedium data-testid="meta" color="#E6F7FF" lineHeight="22px">
          Process to upgrade Kraken to new version
        </Text.LightMedium>
        <Flex align="center" gap={8} wrap="wrap">
          {steps.map((step, index) => (
            <Fragment key={step}>
              <Tag
                data-testid={`step${index + 1}`}
                bordered={false}
                className={
                  index === steps.length - 1
                    ? styles.specialTag
                    : styles.basicTag
                }
              >
                {index === steps.length - 1 && <DoneIcon />}
                {step}
              </Tag>
              {index < steps.length - 1 && (
                <RightOutlined style={{ color: "#fff", fontSize: 8 }} />
              )}
            </Fragment>
          ))}
        </Flex>
      </Flex>
      <Flex className={styles.content}>
        {!isFetching && !releases?.length ? (
          <Empty description="No release history" className={styles.empty} />
        ) : (
          <>
            <VersionSelect
              data={releases ?? []}
              selectedVersion={selectedVersion}
              setSelectedVersion={setSelectedVersion}
              // Infinite scroll
              loading={isFetching && !isFetchingNextPage}
              isFetchingNextPage={isFetchingNextPage}
              hasNextPage={hasNextPage}
              onFetchNext={fetchNextPage}
            />
            <VersionDetail data={currentDetailInfo as any} />
          </>
        )}
      </Flex>
    </div>
  );
}
