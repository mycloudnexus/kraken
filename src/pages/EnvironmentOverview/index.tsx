import Text from "@/components/Text";
import { useGetProductEnvs } from "@/hooks/product";
import { toDateTime, toTime } from "@/libs/dayjs";
import { DEFAULT_PRODUCT } from "@/utils/constants/product";
import { MoreOutlined } from "@ant-design/icons";
import { Button, Dropdown, Flex, MenuProps, Spin } from "antd";
import { useCallback, useRef } from "react";
import EnvStatus from "./components/EnvStatus";
import { showModalConfirmRotate } from "./components/ModalConfirmRotateAPIKey";
import { showModalShowNew } from "./components/ModalShowAPIKey";
import styles from "./index.module.scss";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "@/utils/constants/route";

const availableEnvs = ["dev", "production", "uat", "stage"];

const EnvironmentOverview = () => {
  const navigate = useNavigate();
  const { data, isLoading } = useGetProductEnvs(DEFAULT_PRODUCT);
  const modalConfirmRef = useRef<any>();
  const onConfirmRotate = () => () => {
    modalConfirmRef?.current?.destroy();
    showModalShowNew(
      "abcdefefacnakcnabcdefefacnakcnabcdefefacnakcnabcdefefacnakcnabcdefefacnakcnabcdefefacnakcn"
    );
  };
  const dropdownItems: (envId: string, envName: string) => MenuProps["items"] =
    useCallback(
      (envId, envName) => [
        {
          key: "view-log",
          label: "API activity log",
          onClick: () => {
            navigate(ROUTES.ENV_ACTIVITY_LOG(envId));
          },
        },
        {
          key: "view-details",
          label: "View details",
          children: [
            {
              key: "address-validate",
              label: "Address Validation",
            },
            {
              key: "quality",
              label: "Product offering quality",
            },
            {
              key: "quote",
              label: "Quote",
            },
            {
              key: "order",
              label: "Order",
            },
          ],
        },
        {
          key: "refresh-key",
          label: "Rotate API key",
          onClick: () => {
            modalConfirmRef.current = showModalConfirmRotate(
              envName,
              onConfirmRotate()
            );
          },
        },
      ],
      []
    );
  return (
    <Flex vertical gap={14} className={styles.overviewWrapper}>
      <Flex justify="space-between" align="center">
        <Text.Custom size="20px" bold="700">
          Environment Overview
        </Text.Custom>
        <Button type="primary">New deployment</Button>
      </Flex>
      <Spin spinning={isLoading}>
        <Flex gap={36}>
          {availableEnvs.map((env) => {
            const envData = data?.data?.find((item) => item.name === env);
            return (
              <Flex vertical gap={16} key={env} className={styles.overviewItem}>
                <Flex justify="space-between" align="center">
                  <Text.BoldMedium>{env}</Text.BoldMedium>
                  {envData && (
                    <Dropdown
                      menu={{ items: dropdownItems(envData.id, envData.name) }}
                    >
                      <MoreOutlined />
                    </Dropdown>
                  )}
                </Flex>
                <EnvStatus
                  apiKey={undefined}
                  status={undefined}
                  dataPlane={undefined}
                />
                {envData?.createdAt && (
                  <Flex
                    justify="space-between"
                    align="center"
                    className={styles.lastDeloyed}
                  >
                    <Text.NormalSmall style={{ color: "#00000073" }}>
                      Last Deployed at
                    </Text.NormalSmall>
                    <Text.NormalSmall style={{ color: "#00000073" }}>
                      {toDateTime(envData.createdAt, true)} |{" "}
                      {toTime(envData.createdAt)}
                    </Text.NormalSmall>
                  </Flex>
                )}
              </Flex>
            );
          })}
        </Flex>
      </Spin>
    </Flex>
  );
};

export default EnvironmentOverview;
