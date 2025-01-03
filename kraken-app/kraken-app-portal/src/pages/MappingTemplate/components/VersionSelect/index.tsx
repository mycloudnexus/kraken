import { SecondaryText, Text } from "@/components/Text";
import { IReleaseHistory } from "@/utils/types/product.type";
import { DownOutlined } from "@ant-design/icons";
import { Dropdown, Empty, Flex, MenuProps, Spin, Tag } from "antd";
import classNames from "classnames";
import clsx from "clsx";
import dayjs from "dayjs";
import { debounce, isEmpty } from "lodash";
import { useEffect, useRef, useState } from "react";
import { ListVersionSkeleton } from "./ListVersionSkeleton";
import styles from "./index.module.scss";
import { DAY_TIME_FORMAT_NORMAL } from "@/utils/constants/format";

const statusItems: MenuProps["items"] = [
  {
    key: "All",
    label: <Tag className={styles.prodTag}>All</Tag>,
  },
  {
    key: "Not upgraded",
    label: <Tag className={styles.prodTag}>Not upgraded</Tag>,
  },
  {
    key: "Upgrading",
    label: (
      <Tag className={classNames(styles.prodTag, styles.upgrading)}>
        Upgrading
      </Tag>
    ),
  },
  {
    key: "Deprecated",
    label: (
      <Tag className={classNames(styles.prodTag, styles.deprecated)}>
        Deprecated
      </Tag>
    ),
  },
  {
    key: "Upgraded",
    label: (
      <Tag className={classNames(styles.prodTag, styles.upgraded)}>
        Upgraded
      </Tag>
    ),
  },
];

function LoadMoreContent({
  isFetching,
  hasNextPage,
  onFetchNext,
}: Readonly<{
  isFetching?: boolean;
  hasNextPage?: boolean;
  onFetchNext?: () => void;
}>) {
  if (isFetching) return <Spin />;

  if (hasNextPage)
    return (
      <a href="#" onClick={() => hasNextPage && onFetchNext?.()}>
        Load more
      </a>
    );

  return "Nothing more to load";
}

export const VersionSelect = ({
  isFetchingNextPage,
  hasNextPage,
  data,
  selectedVersion,
  setSelectedVersion,
  loading,
  onFetchNext,
}: Readonly<{
  data: IReleaseHistory[];
  selectedVersion: string | null | undefined;
  setSelectedVersion: (templateUpgradeId: string | null) => void;
  // Inifinite scroll
  loading?: boolean;
  isFetchingNextPage?: boolean;
  hasNextPage?: boolean;
  onFetchNext?(): void;
}>) => {
  const listRef = useRef<HTMLDivElement>(null);

  const [filterBy, setFilterBy] = useState("All");

  const fetchNext = debounce(() => onFetchNext?.(), 100);

  const handleScroll = () => {
    const {
      scrollTop = 0,
      scrollHeight = 0,
      clientHeight = 0,
    } = listRef.current ?? {};

    if (
      scrollTop + clientHeight >= scrollHeight &&
      hasNextPage &&
      !isFetchingNextPage
    ) {
      fetchNext();
    }
  };

  const listRelease =
    filterBy === "All" ? data : data.filter((item) => item.status === filterBy);

  useEffect(() => {
    if (!selectedVersion) {
      setSelectedVersion(listRelease[0]?.templateUpgradeId ?? null)
    }
  }, [selectedVersion, listRelease, filterBy])

  return (
    <div className={styles.root}>
      <Flex justify="space-between">
        <Text.NormalMedium data-testid="versionListTitle" lineHeight="22px">
          Releases
        </Text.NormalMedium>

        <Dropdown
          menu={{
            items: statusItems,
            onClick: (info) => {
              setFilterBy(info.key)
              setSelectedVersion(null)
            },
          }}
        >
          <span className={styles.releaseFilter}>
            <Tag
              className={classNames(styles.prodTag, {
                [styles.upgrading]: filterBy === "Upgrading",
                [styles.upgraded]: filterBy === "Upgraded",
                [styles.deprecated]: filterBy === "Deprecated",
              })}
            >
              {filterBy}
            </Tag>
            <DownOutlined />
          </span>
        </Dropdown>
      </Flex>

      <Flex vertical className={styles.versionList} ref={listRef} onScroll={handleScroll}>
        {loading && <ListVersionSkeleton />}

        {!listRelease.length && !loading && (
          <Empty
            className={styles.emptyRelease}
            description={<SecondaryText.LightNormal>No matched release</SecondaryText.LightNormal>} />
        )}

        {listRelease.map((d) => (
          <Flex
            key={d.templateUpgradeId}
            data-testid="releaseVersionItem"
            vertical
            wrap="wrap"
            gap={3}
            className={clsx([
              styles.item,
              !isEmpty(selectedVersion) &&
              d.templateUpgradeId === selectedVersion &&
              styles.selected,
            ])}
            onClick={() => setSelectedVersion(d.templateUpgradeId)}
          >
            <Flex align="center" gap={7}>
              <Text.LightMedium data-testid="releaseVersion" className={styles.releaseVersion}>
                {d.productVersion}
              </Text.LightMedium>
              <SecondaryText.LightSmall data-testid="productSpec" className={styles.productSpec}>
                {d.productSpec}
              </SecondaryText.LightSmall>
              <Tag
                data-testid="releaseStatus"
                color="#F0F2F5"
                className={classNames(styles.prodTag, {
                  [styles.upgrading]: d.status === "Upgrading",
                  [styles.upgraded]: d.status === "Upgraded",
                  [styles.deprecated]: d.status === "Deprecated",
                })}
                style={{ marginLeft: "auto", marginRight: 0 }}
              >
                {d.status ?? "Not upgraded"}
              </Tag>
            </Flex>
            <SecondaryText.LightSmall>
              Released {dayjs(d.publishDate).format(DAY_TIME_FORMAT_NORMAL)}
            </SecondaryText.LightSmall>
          </Flex>
        ))}

        {listRelease.length > 0 && (<Flex
          justify="center"
          style={{
            marginTop: 12,
            color: "var(--text-secondary)",
            fontSize: 12,
          }}
        >
          <LoadMoreContent
            isFetching={isFetchingNextPage && !loading}
            hasNextPage={hasNextPage}
            onFetchNext={onFetchNext}
          />
        </Flex>)}
      </Flex>
    </div>
  );
};
