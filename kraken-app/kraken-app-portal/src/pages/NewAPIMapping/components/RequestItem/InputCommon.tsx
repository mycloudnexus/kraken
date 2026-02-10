import { IRequestMapping } from "@/utils/types/component.type";
import { cloneDeep, set } from "lodash";

export type MappingInputParams = {
  index: number,
  isFocused: boolean,
  requestMapping: any[],
  rightSideInfo: any,
  setRequestMapping: (mapping: any[]) => void,
  setRightSideInfo: (rightSideInfo: any) => void
  
}

export const handleMappingInputChange = (
  item: IRequestMapping,
  changes: { [field in keyof typeof item]?: any },
  mappingInputParams: MappingInputParams) => {
  const newSourceRequest = cloneDeep(mappingInputParams.requestMapping);
  for (const field in changes) {
    set(
      newSourceRequest,
      `[${mappingInputParams.index}].${field}`,
      changes[field as keyof typeof item]
    );
  }

  mappingInputParams.setRequestMapping(newSourceRequest);
  if (mappingInputParams.isFocused && mappingInputParams.rightSideInfo) {
    mappingInputParams.setRightSideInfo({
      ...mappingInputParams.rightSideInfo,
      previousData: newSourceRequest[mappingInputParams.index],
    });
 }
};
