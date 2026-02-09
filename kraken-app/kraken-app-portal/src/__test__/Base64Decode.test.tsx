import { decodeBase64Content } from "@/libs/decode";


describe('base64 decode testing', () => {
  it('decode valid base64 content should success', () => {
    expect(decodeBase64Content()).toEqual("");
    expect(decodeBase64Content({})).toEqual("");
    expect(decodeBase64Content("")).toEqual("");
    expect(decodeBase64Content("e30K")).toEqual("{}");
    expect(decodeBase64Content("data:application/yaml;e30K")).toEqual("{}");
    expect(decodeBase64Content("data:application/x-yaml;e30K")).toEqual("{}");
  })
})
