import { PageLayout } from "@/components/Layout";
import { Fragment, useEffect, useMemo, useState } from "react";
import styles from "./index.module.scss";
import { useInfiniteReleaseHistoryQuery } from "@/hooks/mappingTemplate";
import { useAppStore } from "@/stores/app.store";
import { IReleaseHistory } from "@/utils/types/product.type";
import { RightOutlined } from "@ant-design/icons";
import { Flex, Tag } from "antd";
import { omit } from "lodash";
import { VersionDetail } from "./components/VersionDetail";
import { VersionSelect } from "./components/VersionSelect";
import { Text } from "@/components/Text";
import DoneIcon from "@/assets/icon/upgrade-done.svg";
import ReleaseIcon from "@/assets/release-bg.svg";
import { useGetSystemInfo } from "@/hooks/user";

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
  const { data: info } = useGetSystemInfo();

  const [selectedVersion, setSelectedVersion] = useState<string | null | undefined>(undefined);

  const releases = releaseData?.pages?.flatMap((page) => page.data.data);

  useEffect(() => {
    if (releases?.length && selectedVersion === undefined) {
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
    <PageLayout title={
      <>
        <span>Mapping template release & Upgrade</span>

        <Flex align="center" gap={12}>
          <Text.LightMedium lineHeight="20px" color="#00000073">
            Current
          </Text.LightMedium>
          <Text.LightMedium lineHeight="20px">Control plane</Text.LightMedium>
          <Tag
            data-testid="controlePlaneUpgradeVersion"
            bordered={false}
            className={styles.mappingVersion}
          >
            {info?.controlProductVersion ?? 'N/A'}
          </Tag>
          <Text.LightMedium lineHeight="20px">Stage</Text.LightMedium>
          <Tag
            data-testid="stageUpgradeVersion"
            bordered={false}
            className={styles.mappingVersion}
          >
            {info?.stageProductVersion ?? 'N/A'}
          </Tag>
          <Text.LightMedium lineHeight="20px">
            Production
          </Text.LightMedium>
          <Tag
            data-testid="productionUpgradeVersion"
            bordered={false}
            className={styles.mappingVersion}
          >
            {info?.productionProductVersion ?? 'N/A'}
          </Tag>
        </Flex>
      </>
    }>
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
              Process to upgrade Mapping template to new version
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
        </Flex>
      </div>
    </PageLayout>
  );
};
