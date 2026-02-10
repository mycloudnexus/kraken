import { IRequestMapping } from "@/utils/types/component.type";
import { cloneDeep, set } from "lodash";

export const handleMappingInputChange = (
  changes: { [field in keyof typeof item]?: any },
  index: number,
  isFocused: boolean,
  requestMapping: any[],
  item: IRequestMapping,
  rightSideInfo: any,
  setRequestMapping: (mapping: any[]) => void,
  setRightSideInfo: (rightSideInfo: any) => void) => {
  const newSourceRequest = cloneDeep(requestMapping);
  for (const field in changes) {
    set(
      newSourceRequest,
      `[${index}].${field}`,
      changes[field as keyof typeof item]
    );
  }

  setRequestMapping(newSourceRequest);
  if (isFocused && rightSideInfo) {
    setRightSideInfo({
      ...rightSideInfo,
      previousData: newSourceRequest[index],
    });
 }
};
