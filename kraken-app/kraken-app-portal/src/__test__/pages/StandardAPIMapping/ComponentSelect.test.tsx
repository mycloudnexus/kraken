import { render, screen, fireEvent } from "@testing-library/react";
import { vi } from "vitest";
import { BrowserRouter } from "react-router-dom";
import ComponentSelect from "@/pages/StandardAPIMapping/components/ComponentSelect";

const { mockNavigate } = vi.hoisted(() => ({
  mockNavigate: vi.fn(),
}));

vi.mock("@/stores/mappingUi.store", () => ({
  useMappingUiStore: vi.fn(() => ({ resetUiStore: vi.fn() })),
}));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<any>("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock("@/assets/standardAPIMapping/contact.svg", () => ({ default: () => <span>Icon</span> }));
vi.mock("@/assets/standardAPIMapping/inventory.svg", () => ({ default: () => <span>Icon</span> }));
vi.mock("@/assets/standardAPIMapping/order.svg", () => ({ default: () => <span>Icon</span> }));
vi.mock("@/assets/standardAPIMapping/quote.svg", () => ({ default: () => <span>Icon</span> }));

vi.mock("antd", async () => {
  const actual = await vi.importActual<any>("antd");
  return {
    ...actual,
    Select: ({ options, value, onSelect }: any) => (
      <div className="mock-select-container">
        <div data-testid="select-value-display">{value?.label}</div>

        <input
          data-testid="mock-select-trigger"
          readOnly
          value={value?.value || ""}
          onChange={() => {}}
          style={{ opacity: 0, position: 'absolute' }}
        />

        <div data-testid="select-options">
          {options?.map((opt: any) => (
            <button
              key={opt.value}
              type="button"
              data-testid={`select-option-${opt.value}`}
              onClick={() => onSelect({ value: opt.value })}
              style={{ display: 'block', width: '100%' }}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>
    ),
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

    const selectTrigger = screen.getByTestId("mock-select-trigger");

    fireEvent.mouseDown(selectTrigger);

    const option = screen.getByText("Inventory Component");
    fireEvent.click(option);

    expect(mockNavigate).toHaveBeenCalledWith("/api-mapping/comp-2", {
      state: { productType: undefined },
    });
  });
});