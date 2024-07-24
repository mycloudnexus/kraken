import { IMapperDetails } from '@/utils/types/env.type';
import { InfoCircleFilled } from '@ant-design/icons';
import { Flex, Tag, Tooltip } from 'antd';
import { toUpper } from 'lodash';
import Dot from './Dot';
import styles from '../index.module.scss';

const CollapseItem = ({ data, selectedKey, setActiveSelected }: {
  data: IMapperDetails[];
  selectedKey: string;
  setActiveSelected: (mapItem: IMapperDetails) => void;
}) => (
  <>
    {data.map((el, elIndex) => {
      const isItemActive = selectedKey === el.targetKey;
      const tagLabels = Object.entries(el.mappingMatrix).map(([label, value]) => {
        return { label: label, value: value };
      });

      return (
        <Flex
          key={`${el.targetMapperKey}-${elIndex}`}
          justify='space-between'
          className={`${styles.collapseItem} ${isItemActive ? styles.collapseItemActive : ""}`}
          onClick={() => !isItemActive && setActiveSelected(el)}
        >
          <Flex>
            <Dot vertical />
            {tagLabels.map(({ label, value }, index) => {
              if (index < 2) {
                return (
                  <Tag key={`${el.path}-${label}-${value}-${index}`}>
                    <Flex vertical className={`${styles.tagBadge} ${isItemActive && styles.tagActive}`}>
                      <span>{label}</span>
                      {toUpper(String(value))}
                    </Flex>
                  </Tag>
                );

              } else if (index === 2) {
                return (
                  <Flex vertical justify="center" className={`${styles.tagBadgeExtra}`} key={`${el.path}-extra-${index}`}>
                    <Tooltip title={
                      <Flex>
                        <Flex className={styles.tagTooltipContainer}>
                          {tagLabels.slice(index, tagLabels.length).map(({ label, value }, idx) => (
                            <Flex key={`${el.path}2-${label}-${value}-${idx}`} className={styles.flexItem}>
                              {label}
                              <Tag>
                                {String(value)}
                              </Tag>
                            </Flex>
                          ))}
                        </Flex>
                      </Flex>
                    }>
                      +{tagLabels.length - 2}
                    </Tooltip>
                  </Flex>
                )
              }
            })}
          </Flex>
          {el.mappingStatus === "incomplete" && (
            <Tooltip title="Incomplete mapping">
              <InfoCircleFilled style={{ color: "#FFA940" }} />
            </Tooltip>
          )}
        </Flex>
      );
    })}
  </>
);

export default CollapseItem;
