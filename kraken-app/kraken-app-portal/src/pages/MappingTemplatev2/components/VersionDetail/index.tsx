import RichTextViewer from "@/components/RichTextViewer";
import { Text } from "@/components/Text";
import { useGetMappingTemplateCurrentVersion } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IReleaseHistory } from "@/utils/types/product.type";
import { Flex, Tag } from "antd";
import { get, isEmpty } from "lodash";
import { useMemo, useState } from "react";
import { DetailDrawer } from "../VersionSelect/DetailDrawer";
import { UpgradeProcess } from "./UpgradeProcess";
import styles from "./index.module.scss";

type Props = {
  data: IReleaseHistory;
};

const VersionDetail = ({ data }: Props) => {
  const { currentProduct } = useAppStore();
  const { data: currentVer } =
    useGetMappingTemplateCurrentVersion(currentProduct);

  // To view details of upgrade history
  const [deploymentId, setDeploymentId] = useState<string | null>(null);

  const currentData = useMemo(() => {
    const stage = currentVer?.find(
      (d: any) => d.envName?.toUpperCase?.() === "STAGE"
    );
    const production = currentVer?.find(
      (d: any) => d.envName?.toUpperCase?.() === "PRODUCTION"
    );
    return {
      stage,
      production,
    };
  }, [currentVer]);

  return (
    <>
      <div className={styles.root}>
        {!isEmpty(data) && (
          <>
            <Flex justify="space-between" align="center">
              <Text.NormalLarge data-testid="detailVersion">
                {data.productVersion}
              </Text.NormalLarge>

              <Flex align="center" gap={12}>
                <Text.LightMedium lineHeight="20px" color="#00000073">
                  Current version
                </Text.LightMedium>
                <Text.LightMedium lineHeight="20px">Stage</Text.LightMedium>
                <Tag
                  bordered={false}
                  color="var(--panel-hover-bg)"
                  style={{ color: "var(--primary)" }}
                >
                  {get(currentData, "stage.productVersion") ?? 'N/A'}
                </Tag>
                <Text.LightMedium lineHeight="20px">
                  Production
                </Text.LightMedium>
                <Tag
                  bordered={false}
                  color="var(--panel-hover-bg)"
                  style={{ color: "var(--primary)" }}
                >
                  {get(currentData, "production.productVersion") ?? 'N/A'}
                </Tag>
              </Flex>
            </Flex>

            <div className={styles.container}>
              <Flex vertical className={styles.flexOne}>
                <Text.NormalLarge
                  data-testid="releaseNoteTitle"
                  className={styles.title}
                >
                  Release note
                </Text.NormalLarge>
                <RichTextViewer
                  data-testid="releaseNote"
                  className={styles.content}
                  text={data.description}
                />
              </Flex>
              <UpgradeProcess
                release={data}
              // To view details of upgrade history
                onViewDetail={setDeploymentId}
              />
            </div>
          </>
        )}
      </div>

      <DetailDrawer
        open={Boolean(deploymentId)}
        deploymentId={deploymentId}
      // To view details of upgrade history
        onClose={() => setDeploymentId(null)}
      />
    </>
  );
};

export default VersionDetail;
