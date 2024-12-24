import { PresetStatusColorType } from 'antd/es/_util/colors';

export const getStatusBadge = (value: string): PresetStatusColorType => {
  let result;
  switch (value) {
    case "DONE":
      result = "success"
      break;
    case "FAILED":
      result = "error"
      break;
    case "IN PROGRESS":
      result = "processing";
      break;
    default:
      result = "default";
      break;
  }
  return result as PresetStatusColorType;
}