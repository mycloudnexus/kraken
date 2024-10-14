import { IMapperDetails } from "@/utils/types/env.type";
import { DownOutlined, InfoCircleOutlined, UpOutlined } from "@ant-design/icons";
import { Badge, Flex, Tooltip } from "antd";
import styles from "../index.module.scss";
import MappingMatrix from "@/components/MappingMatrix";
import { useState, useLayoutEffect, useRef } from 'react';

type CollapseItemProps = {
  data: IMapperDetails[];
  selectedKey: string;
  setActiveSelected: (mapItem: IMapperDetails) => void;
};

const CollapseItem = ({ data, selectedKey, setActiveSelected }: CollapseItemProps) => {
  const ref = useRef<any>();
  const [showMore, setShowMore] = useState(false);
  const [showLink, setShowLink] = useState(false);
  useLayoutEffect(() => {
    if (!ref?.current) return

    if (ref.current.clientHeight > 480) {
      setShowLink(true);
    }
  }, [ref]);

  const onClickMore = () => {
    setShowMore(!showMore);
    if (ref.current) ref.current.scrollTo(0, 0)
  };
  return (
    <>
      <Flex ref={ref} vertical className={(showLink && !showMore) ? styles.lessContent : ''}>
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

      </Flex>
      {showLink && (
        <Flex onClick={onClickMore} className={styles.showMoreLess} gap={8}>
          {showMore ? <><UpOutlined /> Collapse</> : <> <DownOutlined /> See more</>}
        </Flex>
      )}
    </>
  );
};

export default CollapseItem;
