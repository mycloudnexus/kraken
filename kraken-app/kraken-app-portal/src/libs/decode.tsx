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

  if (typeof content !== "string" && typeof content !== "number") {
    console.log("invalid base64 content");
    return "";
  }

  try {
    let str = content as string;
    console.log("parsing content");
    let text = str.split(',').pop() as string;
    console.log("parsing content done");
    return decode(text);
  } catch (e) {
    console.error("invalide base64 string");
    throw e;
  }
};