import { fireEvent, render } from "@/__test__/utils";
import { ApiCard } from "@/components/ApiMapping";
import { ApiListSkeleton } from "@/pages/MappingTemplatev2/UpgradePlane/components/ApiListSkeleton";
import { DeprecatedModal } from "@/pages/MappingTemplatev2/UpgradePlane/components/DeprecatedModal";
import { IncompatibleMappingModal } from "@/pages/MappingTemplatev2/UpgradePlane/components/IncompatibleMappingModal";
import { StartUpgradeModal } from "@/pages/MappingTemplatev2/UpgradePlane/components/StartUpgradeModal";
import { DetailDrawer } from "@/pages/MappingTemplatev2/components/VersionSelect/DetailDrawer";
import { ListVersionSkeleton } from "@/pages/MappingTemplatev2/components/VersionSelect/ListVersionSkeleton";

describe("components used in mapping template v2 pages", () => {
  it("should render deprecated warning modal", () => {
    const handleCancel = vi.fn();
    const handleOk = vi.fn();

    const { getByTestId, getAllByRole } = render(
      <DeprecatedModal open onCancel={handleCancel} onOk={handleOk} />
    );
    expect(getByTestId("title")).toHaveTextContent(
      "This mapping template is depreacted"
    );
    expect(getByTestId("meta")).toHaveTextContent(
      "A newer version mapping template started to upgarde."
    );

    const buttons = getAllByRole("button");
    expect(buttons.length).toBe(2);

    // OK button
    expect(buttons[1]).toHaveTextContent("Got it");
    fireEvent.click(buttons[1]);
    expect(handleOk).toHaveBeenCalledTimes(1);

    fireEvent.click(buttons[0]);
    expect(handleCancel).toHaveBeenCalledTimes(1);
  });

  it("should render stage version incompatible warning modal", () => {
    const handleCancel = vi.fn();
    const handleOk = vi.fn();

    const { getByTestId, getAllByRole } = render(
      <IncompatibleMappingModal open onCancel={handleCancel} onOk={handleOk} />
    );
    expect(getByTestId("title")).toHaveTextContent(
      "Kraken version running in stage data plane is not compatible with this mapping template"
    );
    expect(getByTestId("meta")).toHaveTextContent(
      "Please ensure to upgrade kraken in stage data plane to a compatible version first and test all the running use cases before moving to production data plane upgrade."
    );

    const buttons = getAllByRole("button");
    expect(buttons.length).toBe(2);

    // OK button
    expect(buttons[1]).toHaveTextContent("Got it");
    fireEvent.click(buttons[1]);
    expect(handleOk).toHaveBeenCalledTimes(1);

    fireEvent.click(buttons[0]);
    expect(handleCancel).toHaveBeenCalledTimes(1);
  });

  it("should render upgrade confirm modal", () => {
    const handleCancel = vi.fn();
    const handleOk = vi.fn();

    const { getByTestId, getAllByRole } = render(
      <StartUpgradeModal open onCancel={handleCancel} onOk={handleOk} />
    );
    expect(getByTestId("title")).toHaveTextContent(
      "Are you sure to start upgrade now?"
    );
    expect(getByTestId("meta")).toHaveTextContent(
      "Upgrade may take a while. You will not be able to change the API mapping configurations or perform new deployment during the process. Continue?"
    );

    const buttons = getAllByRole("button");
    expect(buttons.length).toBe(3);

    // Cancel button
    expect(buttons[1]).toHaveTextContent("Cancel");

    // OK button
    expect(buttons[2]).toHaveTextContent("Yes, continue");
    fireEvent.click(buttons[2]);
    expect(handleOk).toHaveBeenCalledTimes(1);

    fireEvent.click(buttons[1]);
    expect(handleCancel).toHaveBeenCalledTimes(1);
  });

  it("should render upgrade detail drawer", () => {
    const { getByText, getByTestId } = render(<DetailDrawer open />);

    expect(getByTestId("notification")).toHaveTextContent(
      "Following mapping use cases upgraded because of this template upgrade."
    );

    // Table headings
    expect(getByText("Mapping use case")).toBeInTheDocument();
    expect(getByText("Upgrade to")).toBeInTheDocument();
    expect(getByText("Upgrade status")).toBeInTheDocument();
  });

  it("should render api mapping card", () => {
    const handleClick = vi.fn();

    const { getByTestId, getAllByTestId } = render(
      <ApiCard
        apiInstance={
          {
            method: "get",
            path: "/a/b/c/d",
            mappingMatrix: {
              productType: "uni",
              actionType: "delete",
            },
          } as any
        }
        prefix={<span data-testid="apiMappingCardPrefix">Prefix</span>}
        suffix={<span data-testid="apiMappingCardSuffiix">Suffix</span>}
        onClick={handleClick}
      />
    );

    expect(getByTestId("apiMappingCardPrefix")).toHaveTextContent("Prefix");
    expect(getByTestId("apiMappingCardSuffiix")).toHaveTextContent("Suffix");

    expect(getByTestId("method")).toHaveTextContent("GET");
    expect(getByTestId("apiPath")).toHaveTextContent("/c/d");

    const mappingTypes = getAllByTestId("mappingType");
    const mappingValues = getAllByTestId("mappingValue");
    expect(mappingTypes).toHaveLength(2);
    expect(mappingValues).toHaveLength(2);

    expect(mappingTypes[0]).toHaveTextContent("product Type");
    expect(mappingValues[0]).toHaveTextContent("UNI");
    expect(mappingTypes[1]).toHaveTextContent("action Type");
    expect(mappingValues[1]).toHaveTextContent("DELETE");
  });

  it("should render api list loading skeleton", () => {
    const { container } = render(<ApiListSkeleton />);
    expect(container).toBeInTheDocument();
  });

  it("should render mapping version list loading skeleton", () => {
    const { container } = render(<ListVersionSkeleton />);
    expect(container).toBeInTheDocument();
  });
});
