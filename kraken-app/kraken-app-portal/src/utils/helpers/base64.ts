import { decode } from "js-base64";


export const decodeFileContent = (content: string): string => {
  return decode((content as string).split(',').pop() as string);
};