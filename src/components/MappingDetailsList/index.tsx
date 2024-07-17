import groupByPath from '@/utils/helpers/groupByPath';
import { IMapperDetails } from '@/utils/types/env.type';
import { Collapse, Flex, Tag, Tooltip } from 'antd';
import { toUpper, get } from 'lodash';
import { useMemo, useState } from 'react';
import RequestMethod from '../Method';
import styles from './index.module.scss';
import { InfoCircleFilled } from '@ant-design/icons';

const Dot = ({ vertical }: { vertical?: boolean }) => (
  <div className={vertical ? styles.dottedLine : styles.dot} />
)

const CollapseItem = ({ data, isActive, setActiveSelected }: {
  data: IMapperDetails[];
  isActive: boolean;
  setActiveSelected: () => void;
}) => {
  const keys = Object.keys(data);
  const mapDetails = keys.map(item => (data[Number(item)])).flat() as Array<IMapperDetails>
  return <>
    {mapDetails.map(el => (
      <Flex justify='space-between' className={styles.collapseItem} onClick={setActiveSelected} key={el.targetMapperKey}>
        <Flex>
          {isActive &&
            <Dot vertical />
          }
          {el.productType &&
            <Tag>
              {toUpper(el.productType)}
            </Tag>
          }
          {el.actionType &&
            <Tag>
              {toUpper(el.actionType)}
            </Tag>
          }
        </Flex>
        {el.mappingStatus === "incomplete" &&
          <Tooltip title="Incomplete mapping">
            <InfoCircleFilled style={{ color: "#FF3864" }} />
          </Tooltip>
        }

      </Flex>
    ))}
  </>
}

const CollapseLabel = ({ size, isActive, labelProps, }: { size: number, isActive: boolean, labelProps: { method: string, path: string } }) => (
  <Flex className={styles.labelWrapper}>
    {isActive && <Dot />}
    <RequestMethod method={labelProps.method} />
    {labelProps.path} {`(${size})`}
  </Flex>
)

type MappingDetailsListProps = {
  detailDataMapping: IMapperDetails | undefined;
  setActiveSelected: () => void;
};

const MappingDetailsList = ({ detailDataMapping, setActiveSelected }: MappingDetailsListProps) => {
  const [selectedHeader, setSelectedHeader] = useState<string[] | string>([]);
  const listMapping = useMemo(() => {
    const groupedPaths = groupByPath(get(detailDataMapping, "details", []))
    const object = Object.keys(groupedPaths).map(path => {
      const method = groupedPaths[path][0].method
      const labelProps = { method, path }
      const isActive = selectedHeader.includes(path);
      return {
        key: path,
        label: (<CollapseLabel labelProps={labelProps} size={groupedPaths[path].length} isActive={isActive} />),
        children: (<CollapseItem data={groupedPaths[path]} setActiveSelected={setActiveSelected} isActive={isActive} />),
      }
    })
    return object;
  }, [detailDataMapping, setActiveSelected, selectedHeader]);

  const handleChange = (e: string | string[]) => {
    setSelectedHeader(e);
  }
  return (
    <Collapse
      activeKey={selectedHeader}
      onChange={handleChange}
      className={styles.collapseBox}
      bordered
      ghost
      expandIconPosition="end"
      items={listMapping}
    />
  );
};

export default MappingDetailsList;
