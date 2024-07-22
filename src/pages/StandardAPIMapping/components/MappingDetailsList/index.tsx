import groupByPath from '@/utils/helpers/groupByPath';
import { IMapperDetails } from '@/utils/types/env.type';
import { Collapse, Spin } from 'antd';
import { get } from 'lodash';
import { useMemo, useState, useCallback } from 'react';
import styles from './index.module.scss';
import { CollapseItem, CollapseLabel } from './components';


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
  }, [detailDataMapping, selectedHeader, selectedKey, setActiveSelected]);

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

export default MappingDetailsList;
