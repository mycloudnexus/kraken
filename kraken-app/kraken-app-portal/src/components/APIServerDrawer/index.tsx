import TitleIcon from "@/assets/title-icon.svg";
import {
  useGetComponentListAPI,
  useGetComponentSpecDetails,
} from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { IUnifiedAsset } from "@/utils/types/common.type";
import { IComponent } from "@/utils/types/component.type";
import { CloseOutlined } from "@ant-design/icons";
import { Flex, Drawer, Spin, Table, Button, Tooltip } from "antd";
import { omit, startCase } from "lodash";
import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import MappingMatrix from "../MappingMatrix";
import RequestMethod from "../Method";
import { Text } from "../Text";
import TrimmedPath from "../TrimmedPath";
import styles from "./index.module.scss";

type Props = {
  isOpen: boolean;
  onClose?: () => void;
  item: IComponent | IUnifiedAsset | undefined;
};

const APIServerDrawer = ({ item, isOpen, onClose }: Props) => {
  const { currentProduct } = useAppStore();
  const { data: componentList } = useGetComponentListAPI(currentProduct);

  const findLinkedComponent = (targetAssetKey: string): string | null => {
    const item = componentList?.data?.find(
      (item: { links: { targetAssetKey: string }[] }) =>
        item.links?.some((link) => link.targetAssetKey === targetAssetKey)
    );
    return item?.metadata?.key || null;
  };

  const navigate = useNavigate();
  const { data: componentDetail, isLoading } = useGetComponentSpecDetails(
    currentProduct,
    item?.metadata?.key ?? ""
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
        render: (item: Record<string, any>) => {
          const linkKey = findLinkedComponent(item.metadata.key);
          return (
            <Flex justify="start" align="center">
              <RequestMethod method={item.facets.trigger?.method} />

              <Tooltip title={linkKey ? null : "This use case does not exist."}>
                <Button
                  role="none"
                  type="link"
                  onClick={() =>
                    linkKey ? navigate(`/api-mapping/${linkKey}`) : null
                  }
                >
                  <TrimmedPath
                    path={item.facets.trigger.path}
                    trimLevel={2}
                    color={linkKey ? "#2962FF" : "rgba(0,0,0,0.45)"}
                  />
                </Button>
              </Tooltip>

              <MappingMatrix
                mappingMatrix={omit(item.facets.trigger, ["method", "path"])}
              />
            </Flex>
          );
        },
      },
    ],
    [navigate, findLinkedComponent]
  );

  return (
    <Drawer
      zIndex={1100}
      width={"80vw"}
      closable={false}
      maskClosable
      open={isOpen}
      onClose={onClose}
      title={
        <Flex justify="space-between">
          <Text.NormalLarge>Check details</Text.NormalLarge>
          <CloseOutlined onClick={onClose} style={{ color: "#00000073" }} />
        </Flex>
      }
    >
      <Spin spinning={isLoading}>
        <div className={styles.wrapper}>
          <Flex align="flex-start" vertical gap={12}>
            <Flex
              vertical
              gap={30}
              align="flex-start"
              className={styles.fullWidth}
            >
              {componentDetail &&
                Object.keys(componentDetail.endpointUsage).map((key, index) => (
                  <Flex
                    key={`${key}-${index}`}
                    vertical
                    className={styles.fullWidth}
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
                        rowKey='id'
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
