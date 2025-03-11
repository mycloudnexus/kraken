import { usePathQuery } from "@/hooks/usePathQuery";
import { useMappingUiStore } from "@/stores/mappingUi.store";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { GroupedByPath } from "@/utils/helpers/groupByPath";
import { IMapperDetails } from "@/utils/types/env.type";
import { Select, Spin } from "antd";
import { useMemo, useEffect, useCallback } from "react";
import DropdownOption from "./components/DropdownOption";

type MappingDetailsListProps = {
  groupedPaths: GroupedByPath;
  setActiveSelected: (mapItem: IMapperDetails) => void;
};

type Option = {
  label: JSX.Element;
  value: string;
  item: IMapperDetails;
};

const MappingDetailsList = ({
  groupedPaths,
  setActiveSelected,
}: MappingDetailsListProps) => {
  // Grab targetMapperKey in url's query, select the corresponding mapping
  const query = usePathQuery();

  const { activePath, setSelectedKey, setActivePath, setActiveTab } =
    useMappingUiStore();
  const { setRightSideInfo } = useNewApiMappingStore();

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
          item,
        });
      }
    }
    return list;
  }, [groupedPaths]);

  const handleChange = (_: string, option: Option | Option[]) => {
    if (Array.isArray(option)) return;
    handleSelection(option.item);
  };

  return (
    <Spin spinning={!groupedPaths}>
      <Select
        options={optionsList}
        defaultValue={optionsList[0].value}
        variant="borderless"
        onChange={handleChange}
      />
    </Spin>
  );
};

export default MappingDetailsList;
