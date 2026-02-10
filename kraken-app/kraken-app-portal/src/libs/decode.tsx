import { decode } from "js-base64";

export const decodeBase64Content = (content?: unknown): string => {
  console.log("decoding bas463 content...");
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
  console.log("checking basic: pass");

  try {
    let str = content as string;
    console.log("parsing content");
    if (str.startsWith("data:application/yaml;")) {
      console.log("parsing content with application/yaml");
      if (str.startsWith("data:application/yaml;base64,")) {
        console.log("validation pass");
      } else {
        console.error("validation failed");
        throw Error("invalid format");
      }
    } else if (str.startsWith("data:application/x-yaml;")) {
      console.log("parsing content with application/x-yaml");
      if (str.startsWith("data:application/x-yaml;base64,")) {
        console.log("validation pass");
      } else {
        console.error("validation failed");
        throw Error("invalid format");
      }
    } else {
      console.log("parsing content, raw base64 content, validation pass");
    }
    let text = str.split(',').pop() as string;
    console.log("parsing content: done");
    return decode(text);
  } catch (e) {
    console.error("invalide base64 string");
    throw e;
  }
};