import Flex from "@/components/Flex";
import { useMemo } from "react";
import styles from "../../index.module.scss";
import { DeploymentEnv } from "./DeploymentEnv";

type Props = {
  loading?: boolean;
  runningData: any[];
};

const DeploymentInfo = ({ loading, runningData }: Props) => {
  const { stage, production } = useMemo(() => {
    const stage = runningData?.find(
      (item: any) => item?.envName?.toLowerCase?.() === "stage"
    );
    const production = runningData?.find(
      (item: any) => item?.envName?.toLowerCase?.() === "production"
    );
    return { stage, production };
  }, [runningData]);

  return (
    <Flex justifyContent="flex-end" className={styles.versionWrapper} gap={16}>
      <DeploymentEnv deployment={stage} loading={loading} />
      <DeploymentEnv deployment={production} loading={loading} />
    </Flex>
  );
};

export default DeploymentInfo;
