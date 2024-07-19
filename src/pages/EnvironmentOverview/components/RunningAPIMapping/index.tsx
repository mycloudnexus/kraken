import RequestMethod from "@/components/Method";
import Text from "@/components/Text";
import { useGetRunningAPIList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IEnv } from "@/utils/types/env.type";
import { Flex, Table, Typography } from "antd";
import { useCallback, useMemo } from "react";
import styles from "./index.module.scss";

type Props = {
  env?: IEnv;
};

const RunningAPIMapping = ({ env }: Props) => {
  const { currentProduct } = useAppStore();
  const { data, isLoading } = useGetRunningAPIList(currentProduct, {
    envId: env?.id,
    orderBy: "createdAt",
    direction: "DESC",
    page: 0,
    size: 100,
  });
  const renderTextType = useCallback((type: string) => {
    switch (type) {
      case "uni":
        return "UNI";
      case "access_e_line":
        return "Access E-line";
      default:
        return type;
    }
  }, []);
  const columns = useMemo(
    () => [
      {
        title: "Component",
        dataIndex: "componentName",
        width: 340,
      },
      env?.name?.toLocaleLowerCase?.() === "stage"
        ? {
            title: "API mappings",
            dataIndex: "",
            render: (item: any) => (
              <Flex gap={10} align="center">
                {!!item.medthod && <RequestMethod method={item?.method} />}
                <Typography.Text
                  style={{ color: "#2962FF" }}
                  ellipsis={{ tooltip: item?.path }}
                >
                  {item?.path}
                </Typography.Text>
                <Flex align="center" gap={8}>
                  <div className={styles.tagInfo}>
                    {renderTextType(item.productType)}
                  </div>
                  {item.actionType ? (
                    <div
                      className={styles.tagInfo}
                      style={{ textTransform: "capitalize" }}
                    >
                      {item.actionType}
                    </div>
                  ) : null}
                </Flex>
              </Flex>
            ),
          }
        : {},
      {
        title: "Deployed time",
        dataIndex: "createAt",
        width: 300,
      },
      {
        title: "Action",
        dataIndex: "diffWithStage",
        width: 340,
        render: (diffWithStage: boolean) =>
          !diffWithStage ? (
            <Text.LightMedium color="#00000073">
              Same with running{" "}
              {env?.name?.toLowerCase() === "stage"
                ? "API mapping"
                : "component"}
            </Text.LightMedium>
          ) : (
            <Text.LightMedium color="#2962FF" style={{ cursor: "pointer" }}>
              View difference
            </Text.LightMedium>
          ),
      },
    ],
    [env, renderTextType]
  );
  return (
    <div>
      <Table
        columns={columns}
        loading={isLoading}
        dataSource={data}
        pagination={false}
        rowKey={(item) => JSON.stringify(item)}
      />
    </div>
  );
};

export default RunningAPIMapping;
