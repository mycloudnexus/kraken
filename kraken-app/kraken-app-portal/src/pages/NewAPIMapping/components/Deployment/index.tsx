import { Text } from "@/components/Text";
import { IApiMapperDeployment } from "@/utils/types/product.type";
import { Flex } from "antd";
import { useMemo } from "react";
import { DeploymentStatus } from "./DeploymentStatus";

export function Deployment({
  deploymentData,
  loading,
}: Readonly<{ deploymentData?: IApiMapperDeployment[]; loading?: boolean }>) {
  const { stage, production } = useMemo(() => {
    const stage = deploymentData?.find(
      (item: any) => item?.envName?.toLowerCase?.() === "stage"
    );
    const production = deploymentData?.find(
      (item: any) => item?.envName?.toLowerCase?.() === "production"
    );
    return { stage, production };
  }, [deploymentData]);

  return (
    <Flex align="center" gap={12}>
      <Text.LightSmall data-testid="deploymentTitle" color="#00000073">
        Last deployment
      </Text.LightSmall>

      <DeploymentStatus deployment={stage} loading={loading} />
      <DeploymentStatus deployment={production} loading={loading} />
    </Flex>
  );
}
