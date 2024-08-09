import { IMapperDetails } from "@/utils/types/env.type";
import { InfoCircleOutlined } from "@ant-design/icons";
import { Badge, Flex, Tooltip } from "antd";
import styles from "../index.module.scss";
import MappingMatrix from "@/components/MappingMatrix";

type CollapseItemProps = {
  data: IMapperDetails[];
  selectedKey: string;
  setActiveSelected: (mapItem: IMapperDetails) => void;
};

const CollapseItem = ({ data, selectedKey, setActiveSelected }: CollapseItemProps) => {
  return (
    <>
      {data.map((el, elIndex) => {
        const isItemActive = selectedKey === el.targetKey;
        return (
          <Flex
            key={`${el.targetMapperKey}-${elIndex}`}
            justify="space-between"
            className={`${styles.collapseItem} ${isItemActive ? styles.collapseItemActive : ""}`}
            onClick={() => !isItemActive && setActiveSelected(el)}
          >
            <Flex className={styles.collapseMappingMatrix}>
              <MappingMatrix
                extraKey={el.path}
                mappingMatrix={el.mappingMatrix}
                isItemActive={isItemActive}
              />
            </Flex>
            <Flex justify="flex-end" wrap="wrap" align="center">
              {el.mappingStatus === "incomplete" && (
                <Tooltip title="Incomplete mapping">
                  <Badge status='warning' />
                </Tooltip>
              )}
              {!el.requiredMapping && (
                <Tooltip title="No mapping required">
                  <InfoCircleOutlined style={{ color: "#00000073" }} />
                </Tooltip>
              )}
            </Flex>
          </Flex>
        );
      })}
    </>
  );
};

export default CollapseItem;
