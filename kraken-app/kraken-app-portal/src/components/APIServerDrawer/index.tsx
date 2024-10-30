import TitleIcon from "@/assets/title-icon.svg";
import { useGetComponentSpecDetails } from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { CloseOutlined } from "@ant-design/icons";
import { Flex, Drawer, Spin, Table } from "antd";
import { omit, startCase } from "lodash";
import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import MappingMatrix from "../MappingMatrix";
import RequestMethod from "../Method";
import { Text } from "../Text";
import TrimmedPath from "../TrimmedPath";

type Props = {
  isOpen: boolean;
  onClose?: () => void;
  componentId: string;
};

const APIServerDrawer = ({ componentId, isOpen, onClose }: Props) => {
  const { currentProduct } = useAppStore();
  const navigate = useNavigate();
  const { data: componentDetail, isLoading } = useGetComponentSpecDetails(
    currentProduct,
    componentId
  );

  const columns = useMemo(
    () => [
      {
        title: "Endpoint",
        dataIndex: "facets",
        width: "50%",
        render: (facets: Record<string, any>) => (
          <Flex justify="start">
            <RequestMethod method={facets.endpoints[0]?.method} />
            <TrimmedPath path={facets.endpoints[0]?.path} trimLevel={8} />
          </Flex>
        ),
      },
      {
        title: "Mapping use case",
        render: (item: Record<string, any>) => (
          <Flex justify="start">
            <RequestMethod method={item.facets.trigger?.method} />
            <Text.LightMedium
              role="none"
              color="#2962FF"
              style={{ cursor: "pointer" }}
              onClick={() => navigate(`/api-mapping/${item.metadata.key}`)}
            >
              <TrimmedPath
                path={item.facets.trigger.path}
                trimLevel={2}
                color="blue"
              />
            </Text.LightMedium>

            <MappingMatrix
              mappingMatrix={omit(item.facets.trigger, ["method", "path"])}
            />
          </Flex>
        ),
      },
    ],
    []
  );

  return (
    <Drawer
      zIndex={1100}
      width={"80vw"}
      closable={false}
      open={isOpen}
      title={
        <Flex justify="space-between">
          <Text.NormalLarge>Check details</Text.NormalLarge>
          <CloseOutlined onClick={onClose} style={{ color: "#00000073" }} />
        </Flex>
      }
    >
      <Spin spinning={isLoading}>
        <div
          style={{
            flex: 1,
            display: "flex",
            flexDirection: "column",
            gap: 24,
            width: "100%",
            boxSizing: "border-box",
          }}
        >
          <Flex align="flex-start" vertical gap={12}>
            <Flex
              vertical
              gap={30}
              align="flex-start"
              style={{ width: "100%" }}
            >
              {componentDetail &&
                Object.keys(componentDetail.endpointUsage).map((key, index) => (
                  <Flex
                    key={`${key}-${index}`}
                    vertical
                    style={{ width: "100%" }}
                    gap={20}
                  >
                    <Flex gap={12} justify="flex-start" align="center">
                      <TitleIcon />
                      <Text.NormalLarge>
                        Endpoints used in {startCase(key)}
                      </Text.NormalLarge>
                    </Flex>
                    {componentDetail.endpointUsage[
                      key as keyof typeof componentDetail.endpointUsage
                    ].length > 0 ? (
                      <Table
                        columns={columns}
                        dataSource={
                          componentDetail.endpointUsage[
                            key as keyof typeof componentDetail.endpointUsage
                          ]
                        }
                        pagination={false}
                      />
                    ) : (
                      <Text.LightMedium style={{ color: "rgba(0,0,0,0.45)" }}>
                        No any endpoints used
                      </Text.LightMedium>
                    )}
                  </Flex>
                ))}
            </Flex>
          </Flex>
        </div>
      </Spin>
    </Drawer>
  );
};

export default APIServerDrawer;
