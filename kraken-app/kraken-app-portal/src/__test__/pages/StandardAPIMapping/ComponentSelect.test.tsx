import { render, screen, fireEvent } from "@testing-library/react";
import { vi } from "vitest";
import { BrowserRouter } from "react-router-dom";
import ComponentSelect from "@/pages/StandardAPIMapping/components/ComponentSelect";

vi.mock("@/stores/mappingUi.store", () => ({
  useMappingUiStore: vi.fn(() => ({ resetUiStore: vi.fn() })),
}));

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe("ComponentSelect", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mockComponentList = {
    data: [
      {
        metadata: { key: "comp-1", name: "Order Component" },
        facets: { supportedProductTypesAndActions: [{}] },
      },
      {
        metadata: { key: "comp-2", name: "Inventory Component" },
        facets: { supportedProductTypesAndActions: [{}] },
      },
    ],
  };

  it("renders the ComponentSelect correctly", () => {
    render(
      <BrowserRouter>
        <ComponentSelect componentList={mockComponentList} componentName="Order Component" />
      </BrowserRouter>
    );

    expect(screen.getByText("Order Component")).toBeInTheDocument();
  });

  it("calls navigate and resets store on selection", () => {
    render(
      <BrowserRouter>
        <ComponentSelect componentList={mockComponentList} componentName="Order Component" />
      </BrowserRouter>
    );

    const select = screen.getByRole("combobox");
    fireEvent.mouseDown(select);
    const option = screen.getByText("Inventory Component");
    fireEvent.click(option);

    expect(mockNavigate).toHaveBeenCalledWith("/api-mapping/comp-2", {
      state: { productType: undefined },
    });
  });
});
