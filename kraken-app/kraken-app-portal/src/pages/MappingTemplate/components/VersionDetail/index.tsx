import RichTextViewer from "@/components/RichTextViewer";
import { SecondaryText, Text } from "@/components/Text";
import { IReleaseHistory } from "@/utils/types/product.type";
import { Flex } from "antd";
import { useState } from "react";
import { DetailDrawer } from "../VersionSelect/DetailDrawer";
import { UpgradeProcess } from "./UpgradeProcess";
import styles from "./index.module.scss";

export function VersionDetail({ data }: Readonly<{
  data?: IReleaseHistory;
}>) {
  // To view details of upgrade history
  const [deploymentId, setDeploymentId] = useState<string | null>(null);

  return (
    <>
      <div className={styles.root}>
        <Text.NormalLarge data-testid="detailVersion" style={{ minHeight: 19 }}>
          {data?.productVersion}
        </Text.NormalLarge>

        <div className={styles.container}>
          {!data ? (
            <div className={styles.blank}>
              <SecondaryText.LightNormal data-testid="releaseBlank">No release selected</SecondaryText.LightNormal>
            </div>
          ) : (
            <>
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
            </>
          )}
        </div>
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
