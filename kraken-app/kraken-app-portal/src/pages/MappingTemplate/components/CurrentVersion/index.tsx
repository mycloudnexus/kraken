import { Text } from "@/components/Text";
import { useGetMappingTemplateCurrentVersion } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { Flex, Tag } from "antd";
import { get } from "lodash";
import { useMemo } from "react";

const CurrentVersion = () => {
  const { currentProduct } = useAppStore();
  const { data: currentVer } =
    useGetMappingTemplateCurrentVersion(currentProduct);
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
    <Flex align="center" gap={12}>
      <Text.LightMedium lineHeight="20px" color="#00000073">
        Current version
      </Text.LightMedium>
      <Flex align="center" gap={8}>
        <Text.LightMedium lineHeight="20px">Stage</Text.LightMedium>
        <Tag bordered={false} color="#DBE4FB" style={{ color: "#2962FF" }}>
          {get(currentData, "stage.productVersion")}
        </Tag>
        <Text.LightMedium lineHeight="20px">Production</Text.LightMedium>
        <Tag bordered={false} color="#DBE4FB" style={{ color: "#2962FF" }}>
          {get(currentData, "production.productVersion")}
        </Tag>
      </Flex>
    </Flex>
  );
};

export default CurrentVersion;
