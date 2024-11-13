import { PageLayout } from "@/components/Layout";
import { Fragment, useEffect, useMemo, useState } from "react";
import styles from "./index.module.scss";
import { useInfiniteReleaseHistoryQuery } from "@/hooks/mappingTemplate";
import { useAppStore } from "@/stores/app.store";
import { IReleaseHistory } from "@/utils/types/product.type";
import { RightOutlined } from "@ant-design/icons";
import { Flex, Tag, Empty } from "antd";
import { omit } from "lodash";
import VersionDetail from "./components/VersionDetail";
import VersionSelect from "./components/VersionSelect";
import { Text } from "@/components/Text";
import DoneIcon from "@/assets/icon/upgrade-done.svg";
import ReleaseIcon from "@/assets/release-bg.svg";

const steps = [
  "Control plane upgrade",
  "Stage data plane upgrade",
  "Test offline",
  "Production data plane upgrade",
  "Upgrade done!",
];


export default function MappingTemplate() {
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
    <PageLayout title="Mapping template release & Upgrade">
      <div className={styles.root}>
        <Flex justify="space-between"
            className={styles.info}>
          <Flex
            vertical
            justify="center"
            gap={16}
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

          <ReleaseIcon />
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
    </PageLayout>
  );
};
