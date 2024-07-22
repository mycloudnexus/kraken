import { IMapperDetails } from '@/utils/types/env.type';
import { InfoCircleFilled } from '@ant-design/icons';
import { Flex, Tag, Tooltip } from 'antd';
import { toUpper } from 'lodash';
import Dot from './Dot';
import styles from '../index.module.scss'

const CollapseItem = ({ data, selectedKey, setActiveSelected }: {
  data: IMapperDetails[];
  selectedKey: string;
  setActiveSelected: (mapItem: IMapperDetails) => void;
}) => (
  <>
    {data.map(el => {
      const isItemActive = selectedKey === el.targetKey
      return (
        <Flex
          key={el.targetMapperKey}
          justify='space-between'
          className={`${styles.collapseItem} ${isItemActive ? styles.collapseItemActive : ""}`}
          onClick={() => !isItemActive && setActiveSelected(el)}
        >
          <Flex>
            <Dot vertical />
            {el.productType && <Tag>{toUpper(el.productType)}</Tag>}
            {el.actionType && <Tag>{toUpper(el.actionType)}</Tag>}
          </Flex>
          {el.mappingStatus === "incomplete" && (
            <Tooltip title="Incomplete mapping">
              <InfoCircleFilled style={{ color: "#FF3864" }} />
            </Tooltip>
          )}
        </Flex>
      )
    })}
  </>
);

export default CollapseItem;