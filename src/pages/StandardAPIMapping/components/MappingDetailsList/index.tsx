import { useMemo, useEffect, useState, useCallback } from "react";
import { Collapse, Spin } from "antd";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import { GroupedByPath } from "@/utils/helpers/groupByPath";
import { IMapperDetails } from "@/utils/types/env.type";
import styles from "./index.module.scss";
import { CollapseItem, CollapseLabel } from "./components";

type MappingDetailsListProps = {
  groupedPaths: GroupedByPath;
  setActiveSelected: (mapItem: IMapperDetails) => void;
};

const MappingDetailsList = ({
  groupedPaths,
  setActiveSelected,
}: MappingDetailsListProps) => {
  const [activeLabel, setActiveLabel] = useState<string[]>(
    Object.keys(groupedPaths)
  );

  const {
    activePath,
    selectedKey,
    setSelectedKey,
    setActivePath,
    setActiveTab,
  } = useMappingUiStore();

  useEffect(() => {
    const headersList = Object.keys(groupedPaths);
    if (headersList.length > 0) {
      const initialMapItem = groupedPaths[headersList[0]][0];
      setSelectedKey(initialMapItem.path);
      setActivePath(initialMapItem.path);
      setActiveSelected(initialMapItem);
    }
  }, []);

  const handleSelection = useCallback(
    (mapItem: IMapperDetails) => {
      setSelectedKey(mapItem.targetKey);
      setActiveSelected(mapItem);
      setActiveTab("request");
    },
    [setActiveSelected, setSelectedKey]
  );

  const listMapping = useMemo(() => {
    return Object.keys(groupedPaths).map((path) => {
      const labelProps = groupedPaths[path][0];
      const isActiveLabel = activeLabel.includes(path);
      const isOneChild = groupedPaths[path].length <= 1;
      const highlighted = groupedPaths[path].some(
        (item) => item.path === activePath
      );

      return {
        key: path,
        showArrow: !isOneChild,
        label: (
          <CollapseLabel
            labelProps={labelProps}
            handleSelection={handleSelection}
            size={groupedPaths[path].length}
            isActive={isActiveLabel}
            isOneChild={isOneChild}
            highlighted={highlighted}
          />
        ),
        children: isOneChild ? null : (
          <CollapseItem
            data={groupedPaths[path]}
            setActiveSelected={handleSelection}
            selectedKey={selectedKey}
          />
        ),
      };
    });
  }, [activeLabel, activePath, groupedPaths, handleSelection, selectedKey]);

  const handleChange = useCallback((e: string[] | string) => {
    setActiveLabel(Array.isArray(e) ? e : [e]);
  }, []);

  return (
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
  );
};

export default MappingDetailsList;
