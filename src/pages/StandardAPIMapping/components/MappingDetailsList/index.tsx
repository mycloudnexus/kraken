import groupByPath from '@/utils/helpers/groupByPath';
import { IMapperDetails } from '@/utils/types/env.type';
import { Collapse, Flex, Spin, Tag, Tooltip } from 'antd';
import { toUpper, get } from 'lodash';
import { useMemo, useState, useCallback, memo } from 'react';
import RequestMethod from '../../../../components/Method';
import styles from './index.module.scss';
import { InfoCircleFilled } from '@ant-design/icons';

const Dot = ({ vertical }: { vertical?: boolean }) => (
  <div className={vertical ? styles.dottedLine : styles.dot} />
);

const CollapseItem = ({ data, selectedKey, setActiveSelected }: {
  data: IMapperDetails[];
  selectedKey: string;
  setActiveSelected: (mapItem: IMapperDetails) => void;
}) => (
  <>
    {data.map(el => (
      <Flex
        key={el.targetMapperKey}
        justify='space-between'
        className={`${styles.collapseItem} ${selectedKey === el.targetKey ? styles.collapseItemActive : ""}`}
        onClick={() => setActiveSelected(el)}
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
    ))}
  </>
);

const CollapseLabel = ({ size, isActive, labelProps }: {
  size: number;
  isActive: boolean;
  labelProps: { method: string; path: string };
}) => (
  <Flex className={styles.labelWrapper}>
    {isActive && <Dot />}
    <RequestMethod method={labelProps.method} />
    .../{labelProps.path.split('/').slice(-3).join('/')} {`(${size})`}
  </Flex>
);

type MappingDetailsListProps = {
  detailDataMapping: IMapperDetails | undefined;
  setActiveSelected: (mapItem: IMapperDetails) => void;
};

const MappingDetailsList = ({ detailDataMapping, setActiveSelected }: MappingDetailsListProps) => {
  const [selectedHeader, setSelectedHeader] = useState<string[] | string>([]);
  const [selectedKey, setSelectedKey] = useState<string>("");

  const listMapping = useMemo(() => {
    const groupedPaths = groupByPath(get(detailDataMapping, "details", []));

    return Object.keys(groupedPaths).map(path => {
      const method = groupedPaths[path][0].method;
      const labelProps = { method, path };
      const isActiveLabel = selectedHeader.includes(path);

      const handleSelection = (mapItem: IMapperDetails) => {
        setSelectedKey(mapItem.targetKey);
        setActiveSelected(mapItem);
      };

      return {
        key: path,
        label: <CollapseLabel labelProps={labelProps} size={groupedPaths[path].length} isActive={isActiveLabel} />,
        children: <CollapseItem data={groupedPaths[path]} setActiveSelected={handleSelection} selectedKey={selectedKey} />,
      };
    });
  }, [detailDataMapping, setActiveSelected, selectedHeader]);

  const handleChange = useCallback((e: string | string[]) => {
    setSelectedHeader(e);
  }, []);

  return (
    <>
      <Spin spinning={!detailDataMapping}>
        <Collapse
          activeKey={selectedHeader}
          onChange={handleChange}
          className={styles.collapseBox}
          bordered
          ghost
          expandIconPosition="end"
          items={listMapping}
        />
      </Spin>
    </>

  );
};

export default memo(MappingDetailsList);
