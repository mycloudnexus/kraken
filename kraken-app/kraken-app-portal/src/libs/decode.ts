import { decode } from "js-base64";

export const decodeBase64Content = (content?: unknown): string => {
  if (!content) {
    console.log("invalid base64 content: null");
    return "";
  }

  if (content === "") {
    console.log("invalid base64 content: blank");
    return "";
  }

  if (typeof content !== "string") {
    console.log("invalid base64 content");
    return "";
  }

  let str = content as string;
  console.log("parsing content");
  let text = str.split(',').pop() as string;
  console.log("parsing content done");
  return decode(text);
};