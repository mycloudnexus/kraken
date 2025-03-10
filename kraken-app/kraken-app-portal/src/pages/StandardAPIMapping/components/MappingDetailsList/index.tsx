import { usePathQuery } from "@/hooks/usePathQuery";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { GroupedByPath } from "@/utils/helpers/groupByPath";
import { IMapperDetails } from "@/utils/types/env.type";
import { Collapse, Select, Spin } from "antd";
import { useMemo, useEffect, useState, useCallback } from "react";
import { CollapseItem, CollapseLabel } from "./components";
import DropdownOption from "./components/DropdownOption";
import styles from "./index.module.scss";

type MappingDetailsListProps = {
  groupedPaths: GroupedByPath;
  setActiveSelected: (mapItem: IMapperDetails) => void;
};

const MappingDetailsList = ({
  groupedPaths,
  setActiveSelected,
}: MappingDetailsListProps) => {
  // Grab targetMapperKey in url's query, select the corresponding mapping
  const query = usePathQuery();

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
  const { setRightSideInfo } = useNewApiMappingStore();

  useEffect(() => {
    setActiveLabel(Object.keys(groupedPaths));
  }, [groupedPaths]);

  const initList = useCallback(() => {
    const apis = Object.values(groupedPaths).flatMap((subPath) => subPath);
    if (apis.length > 0) {
      const targetMapperKey = query.get("targetMapperKey");

      // Auto select the corresponding api
      const initialMapItem = targetMapperKey
        ? (apis.find(
            (mapping) => mapping.targetMapperKey === targetMapperKey
          ) ?? apis[0])
        : apis[0];

      setSelectedKey(initialMapItem.path);
      setActivePath(initialMapItem.path);
      setActiveSelected(initialMapItem);
    }
  }, [activePath]);

  useEffect(() => {
    initList();
  }, [window.location.pathname]);

  const handleSelection = useCallback(
    (mapItem: IMapperDetails) => {
      setRightSideInfo(undefined);
      setActiveSelected(mapItem);
      setActiveTab("request");
    },
    [setActiveSelected, setSelectedKey]
  );

  // const listMapping = useMemo(() => {
  //   return Object.keys(groupedPaths).map((path) => {
  //     const labelProps = groupedPaths[path][0];
  //     const isActiveLabel = activeLabel.includes(path);
  //     const isOneChild = groupedPaths[path].length <= 1;
  //     const highlighted = groupedPaths[path].some(
  //       (item) => item.path === activePath
  //     );

  //     return {
  //       key: path,
  //       showArrow: !isOneChild,
  //       label: (
  //         <CollapseLabel
  //           labelProps={labelProps}
  //           handleSelection={handleSelection}
  //           size={groupedPaths[path].length}
  //           isActive={isActiveLabel}
  //           isOneChild={isOneChild}
  //           highlighted={highlighted}
  //         />
  //       ),
  //       children: isOneChild ? null : (
  //         <CollapseItem
  //           data={groupedPaths[path]}
  //           setActiveSelected={handleSelection}
  //           selectedKey={selectedKey}
  //         />
  //       ),
  //     };
  //   });
  // }, [activeLabel, activePath, groupedPaths, handleSelection, selectedKey]);

  const optionsList = useMemo(() => {
    const list = [];
    for (const path in groupedPaths) {
      for (const item of groupedPaths[path]) {
        const optionProps = {
          ...item,
          size: groupedPaths[path].length,
          value: `${path} ${item.targetKey}`,
        };
        list.push({
          label: <DropdownOption {...optionProps} />,
          value: `${path} ${item.targetKey}`,
        });
      }
    }
    return list;
  }, [groupedPaths]);

  const handleChange = useCallback((e: string[] | string) => {
    setActiveLabel(Array.isArray(e) ? e : [e]);
  }, []);

  // console.log(">>>>>groupedPaths", groupedPaths);

  return (
    <Spin spinning={!groupedPaths}>
      {/* <Collapse
        activeKey={activeLabel}
        onChange={handleChange}
        className={styles.collapseBox}
        bordered
        ghost
        expandIconPosition="end"
        items={listMapping}
      /> */}
      <Select
        options={optionsList}
        defaultValue={optionsList[0].value}
        variant="borderless"
      />
    </Spin>
  );
};

export default MappingDetailsList;
