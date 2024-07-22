import { GroupedByPath } from '@/utils/helpers/groupByPath';
import { IMapperDetails } from '@/utils/types/env.type';
import { Collapse, Spin } from 'antd';
import { useMemo, useEffect, useState, useCallback } from 'react';
import styles from './index.module.scss';
import { CollapseItem, CollapseLabel } from './components';
import { useMappingUiStore } from '@/stores/mappingUi.store';


type MappingDetailsListProps = {
  groupedPaths: GroupedByPath;
  setActiveSelected: (mapItem: IMapperDetails) => void;
};

const MappingDetailsList = ({ groupedPaths, setActiveSelected }: MappingDetailsListProps) => {
  const [activeLabel, setActiveLabel] = useState<Array<string>>(Object.keys(groupedPaths));
  const { selectedKey, setSelectedKey, setActivePath } = useMappingUiStore();

  const listMapping = useMemo(() => {
    return Object.keys(groupedPaths).map(path => {
      const method = groupedPaths[path][0].method;
      const labelProps = { method, path };
      const isActiveLabel = activeLabel.includes(path);

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
  }, [activeLabel, groupedPaths, selectedKey, setActiveSelected, setSelectedKey]);

  const handleChange = useCallback((e: string[] | string) => {
    setActiveLabel(typeof e === "string" ? [e] : e)
  }, []);

  useEffect(() => {
    const headersList = Object.keys(groupedPaths)
    setActiveLabel(headersList)
    if (headersList.length > 0) {
      const mapItem = groupedPaths[headersList[0]][0];
      setSelectedKey(mapItem.targetKey);
      setActivePath(mapItem.path);
      setActiveSelected(groupedPaths[headersList[0]][0])
    }
  }, [])


  return (
    <>
      <Spin spinning={!groupedPaths}>
        <Collapse
          defaultActiveKey={activeLabel}
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
