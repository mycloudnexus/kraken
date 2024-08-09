import RequestMethod from "@/components/Method";
import Text from "@/components/Text";
import { useGetRunningAPIList } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IEnv } from "@/utils/types/env.type";
import { Flex, Table, Typography } from "antd";
import { useCallback, useMemo } from "react";
import dayjs from "dayjs";
import MappingMatrix from "@/components/MappingMatrix";
import { get } from "lodash";

type Props = {
  env?: IEnv;
};

const RunningAPIMapping = ({ env }: Props) => {
  const { currentProduct } = useAppStore();
  const envName = env?.name?.toLocaleLowerCase?.();
  const { data, isLoading } = useGetRunningAPIList(
    currentProduct,
    {
      envId: env?.id,
      orderBy: "createdAt",
      direction: "DESC",
      page: 0,
      size: 100,
    },
    envName
  );

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
    () =>
      [
        {
          title: "Component",
          dataIndex: "componentName",
          width: 340,
        },
        envName === "stage"
          ? {
              dataIndex: "",
              title: "API mappings",
              render: (item: any) => (
                <Flex align="center" gap={10}>
                  <RequestMethod method={item?.method} />
                  <Typography.Text
                    ellipsis={{ tooltip: item?.path }}
                    style={{ color: "#2962FF" }}
                  >
                    {item?.path}
                  </Typography.Text>
                  <Flex gap={8} align="center">
                    <MappingMatrix
                      mappingMatrix={item?.mappingMatrix}
                      extraKey={"item.path"}
                      isItemActive={false}
                    />
                  </Flex>
                </Flex>
              ),
            }
          : {
              title: "Version",
              dataIndex: "version",
            },
        {
          title: "Deployed time",
          dataIndex: "createAt",
          width: 340,
          render: (time: string) =>
            dayjs.utc(time).local().format("YYYY-MM-DD HH:mm:ss"),
        },
        envName === "stage"
          ? {
              title: "Action",
              dataIndex: "diffWithStage",
              width: 300,
              render: (diffWithStage: boolean) =>
                !diffWithStage ? (
                  <Text.LightMedium color="#00000073">
                    Same with running{" "}
                    {envName === "stage" ? "API mapping" : "component"}
                  </Text.LightMedium>
                ) : (
                  <Text.LightMedium
                    color="#2962FF"
                    style={{ cursor: "pointer" }}
                  >
                    View difference
                  </Text.LightMedium>
                ),
            }
          : {},
      ].filter((value) => Object.keys(value).length !== 0),
    [env, renderTextType]
  );

  const columnData = useMemo(() => {
    if (envName !== "production") {
      return data;
    }

    return get(data, "data", []).flatMap((item: any) =>
      item?.components?.map((component: any) => ({
        createAt: item?.createdAt,
        ...component,
      }))
    );
  }, [envName, data]);

  return (
    <div>
      <Table
        columns={columns}
        loading={isLoading}
        dataSource={columnData}
        pagination={false}
        rowKey={(item) => JSON.stringify(item)}
      />
    </div>
  );
};

export default RunningAPIMapping;
