import { IMapperDetails } from "@/utils/types/env.type";
import { InfoCircleFilled, InfoCircleOutlined } from "@ant-design/icons";
import { Flex, Tooltip } from "antd";
import Dot from "./Dot";
import styles from "../index.module.scss";
import MappingMatrix from "@/components/MappingMatrix";

const CollapseItem = ({
  data,
  selectedKey,
  setActiveSelected,
}: {
  data: IMapperDetails[];
  selectedKey: string;
  setActiveSelected: (mapItem: IMapperDetails) => void;
}) => (
  <>
    {data.map((el, elIndex) => {
      const isItemActive = selectedKey === el.targetKey;
      return (
        <Flex
          key={`${el.targetMapperKey}-${elIndex}`}
          justify="space-between"
          className={`${styles.collapseItem} ${
            isItemActive ? styles.collapseItemActive : ""
          }`}
          onClick={() => !isItemActive && setActiveSelected(el)}
        >
          <Flex>
            <Dot vertical />
            <MappingMatrix
              extraKey={el.path}
              mappingMatrix={el.mappingMatrix}
              isItemActive={isItemActive}
            />
          </Flex>
          <Flex justify="flex-end" gap={8}>
            {el.mappingStatus === "incomplete" && (
              <Tooltip title="Incomplete mapping">
                <InfoCircleFilled style={{ color: "#FFA940" }} />
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

export default CollapseItem;
