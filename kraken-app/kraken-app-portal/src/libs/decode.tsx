import { decode } from "js-base64";

export const decodeBase64Content = (content?: unknown): string => {
  console.log("checking: is content null?");
  if (!content && content !== "") {
    console.log("invalid base64 content: null");
    return "";
  }

  console.log("checking: is content empty?");
  if (content === "") {
    console.log("invalid base64 content: empty");
    return "";
  }

  console.log("checking: is content string or number?");
  if (typeof content !== "string" && typeof content !== "number") {
    console.log("invalid base64 content");
    return "";
  }
  console.log("checking: pass");

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