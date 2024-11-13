import RichTextViewer from "@/components/RichTextViewer";
import { Text } from "@/components/Text";
import { IReleaseHistory } from "@/utils/types/product.type";
import { Flex, Tag } from "antd";
import { isEmpty } from "lodash";
import { useState } from "react";
import { DetailDrawer } from "../VersionSelect/DetailDrawer";
import { UpgradeProcess } from "./UpgradeProcess";
import styles from "./index.module.scss";
import { useGetSystemInfo } from "@/hooks/user";

type Props = {
  data: IReleaseHistory;
};

const VersionDetail = ({ data }: Props) => {
  const { data: info } = useGetSystemInfo();

  // To view details of upgrade history
  const [deploymentId, setDeploymentId] = useState<string | null>(null);

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
                  data-testid="controlePlaneUpgradeVersion"
                  bordered={false}
                  color="var(--panel-hover-bg)"
                  style={{ color: "var(--primary)" }}
                >
                  {info?.controlProductVersion ?? 'N/A'}
                </Tag>
                <Text.LightMedium lineHeight="20px">Stage</Text.LightMedium>
                <Tag
                  data-testid="stageUpgradeVersion"
                  bordered={false}
                  color="var(--panel-hover-bg)"
                  style={{ color: "var(--primary)" }}
                >
                  {info?.stageProductVersion ?? 'N/A'}
                </Tag>
                <Text.LightMedium lineHeight="20px">
                  Production
                </Text.LightMedium>
                <Tag
                  data-testid="productionUpgradeVersion"
                  bordered={false}
                  color="var(--panel-hover-bg)"
                  style={{ color: "var(--primary)" }}
                >
                  {info?.productionProductVersion ?? 'N/A'}
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
