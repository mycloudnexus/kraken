import { SourceInput as RequestSourceInput } from "@/pages/NewAPIMapping/components/RequestItem/SourceInput";
import { TargetInput as RequestTargetInput } from "@/pages/NewAPIMapping/components/RequestItem/TargetInput";
import { SourceInput as ResponseSourceInput } from "@/pages/NewAPIMapping/components/ResponseItem/SourceInput";
import { TargetInput as ResponseTargetInput } from "@/pages/NewAPIMapping/components/ResponseItem/TargetInput";
import * as apiMappingHooks from "@/stores/newApiMapping.store";
import { fireEvent, render, waitFor } from "@testing-library/react";

const request = {
  requestMapping: [
    {
      name: "address.validation.city",
      title: "The city that the address is in",
      source: "@{{hello}}",
      target: "@{{world}}",
      description: "",
      replaceStar: true,
      sourceLocation: "",
      targetLocation: "QUERY",
      customizedField: true,
      requiredMapping: false,
    },
  ],
  rightSide: 2,
  rightSideInfo: {
    method: "update",
    previousData: {
      name: "address.validation.streetName",
      title: "Name of the street or other street type",
      source: "@{{hello}}",
      target: "",
      description: "",
      replaceStar: true,
      sourceLocation: "",
      targetLocation: "",
      customizedField: true,
      requiredMapping: false,
    },
    title: "Name of the street or other street type",
  },
  setRightSide: vi.fn(),
  setRightSideInfo: vi.fn(),
  setRequestMapping: vi.fn(),
};

const response = {
  responseMapping: [
    {
      name: "address.validation.city",
      title: "The city that the address is in",
      source: "@{{source}}",
      target: "@{{target}}",
      description: "",
      replaceStar: true,
      sourceLocation: "",
      targetLocation: "PATH",
      customizedField: true,
      requiredMapping: false,
      targetType: "",
      targetValues: [],
    },
  ],
  rightSide: 2,
  setRightSide: vi.fn(),
  setResponseMapping: vi.fn(),
  setActiveSonataResponse: vi.fn(),
  setActiveResponseName: vi.fn(),
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe("NewAPIMapping > request mapping", () => {
  it("should render a source property input", async () => {
    vi.spyOn(apiMappingHooks, "useNewApiMappingStore").mockImplementation(
      vi.fn().mockReturnValue(request)
    );

    const { getByTestId, getByRole, getAllByRole } = render(
      <RequestSourceInput item={request.requestMapping[0]} index={0} />
    );

    const input = getByTestId("sourceInput");
    expect(input).toHaveTextContent(request.requestMapping[0].source);

    fireEvent.click(input);
    expect(request.setRightSide).toHaveBeenCalledTimes(1);
    expect(request.setRightSideInfo).toHaveBeenCalledTimes(1);

    fireEvent.blur(input);

    const btnSelectLoc = getByTestId("btnSelectLocation");
    expect(btnSelectLoc).toHaveTextContent("Please select location");

    fireEvent.click(btnSelectLoc!);

    await waitFor(() => expect(getByRole("menu")).toBeInTheDocument());

    const items = getAllByRole("menuitem");
    expect(items.length).toBe(6); // path, query, body, constant + divider

    expect(items[0]).toHaveTextContent("Hybrid");
    expect(items[1]).toHaveTextContent("Path parameter");
    expect(items[2]).toHaveTextContent("Query parameter");
    expect(items[3]).toHaveTextContent("Request body");
    expect(items[5]).toHaveTextContent("Constant value");

    fireEvent.click(items[2]);
    expect(request.setRequestMapping).toHaveBeenCalledTimes(1);
  });

  it("should render a target property input", async () => {
    vi.spyOn(apiMappingHooks, "useNewApiMappingStore").mockImplementation(
      vi.fn().mockReturnValue(request)
    );

    const { getByTestId } = render(
      <RequestTargetInput item={request.requestMapping[0]} index={0} />
    );

    const input = getByTestId("targetInput");
    expect(input).toHaveTextContent(request.requestMapping[0].target);

    expect(getByTestId("btnSelectLocation")).toHaveTextContent(
      "Query parameter"
    ); // ~ QUERY

    fireEvent.click(input);
    expect(request.setRightSideInfo).toHaveBeenCalledTimes(1);
    expect(request.setRightSide).toHaveBeenCalledTimes(1);

    fireEvent.blur(input);
  });
});

describe("NewAPIMapping > response mapping", () => {
  it("should render a source property input", async () => {
    vi.spyOn(apiMappingHooks, "useNewApiMappingStore").mockImplementation(
      vi.fn().mockReturnValue(response)
    );

    const { getByTestId, getByRole, getAllByRole } = render(
      <ResponseSourceInput item={response.responseMapping[0]} index={0} />
    );

    const input = getByTestId("sourceInput");
    expect(input).toHaveTextContent(response.responseMapping[0].source);

    fireEvent.click(input);
    expect(response.setActiveResponseName).toHaveBeenCalledTimes(1);
    expect(response.setRightSide).toHaveBeenCalledTimes(1);

    fireEvent.blur(input);
    expect(response.setActiveResponseName).toHaveBeenCalledTimes(1);

    const btnSelectLoc = getByTestId("btnSelectLocation");
    expect(btnSelectLoc!).toHaveTextContent("Please select location");

    fireEvent.click(btnSelectLoc!);

    await waitFor(() => expect(getByRole("menu")).toBeInTheDocument());

    const items = getAllByRole("menuitem");
    expect(items.length).toBe(3); // body, constant + divider

    expect(items[0]).toHaveTextContent("Response body");
    expect(items[2]).toHaveTextContent("Constant value");

    fireEvent.click(items[0]);
    expect(response.setResponseMapping).toHaveBeenCalledTimes(1);
  });

  it("should render a target property input", async () => {
    vi.spyOn(apiMappingHooks, "useNewApiMappingStore").mockImplementation(
      vi.fn().mockReturnValue(response)
    );

    const { getByTestId } = render(
      <ResponseTargetInput item={response.responseMapping[0]} index={0} />
    );

    const input = getByTestId("targetInput");
    expect(input).toHaveTextContent(response.responseMapping[0].target);

    expect(getByTestId("btnSelectLocation")).toHaveTextContent(
      "Path parameter"
    ); // ~ PATH

    fireEvent.click(input);
    expect(response.setActiveSonataResponse).toHaveBeenCalledTimes(1);
    expect(response.setRightSide).toHaveBeenCalledTimes(1);

    fireEvent.blur(input);
    expect(response.setActiveSonataResponse).toHaveBeenCalledTimes(1);
  });
});
