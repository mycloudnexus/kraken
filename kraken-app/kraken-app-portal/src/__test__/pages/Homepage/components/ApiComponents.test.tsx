import { render, screen, fireEvent } from "@testing-library/react";
import { vi } from "vitest";
import { BrowserRouter as Router } from "react-router-dom";
import ApiComponents from "@/pages/HomePage/components/ApiComponents";
import ApiComponent from "@/pages/HomePage/components/ApiComponent";
import { IUnifiedAsset } from "@/utils/types/common.type";

const mockOpenDrawer = vi.fn();
const mockNavigate = vi.fn();

vi.mock("react-router-dom", () => ({
  useNavigate: () => mockNavigate,
  BrowserRouter: ({ children }: { children: React.ReactNode }) => (
    <div>{children}</div>
  ),
}));

vi.mock("@/hooks/product", () => ({
  useGetComponentListAPI: () => ({
    data: {
      data: [
        {
          id: "1",
          facets: {
            supportedProductTypesAndActions: [
              { productTypes: ["ACCESS_E_LINE"], actionTypes: ["CREATE"] },
            ],
          },
          links: [],
        },
      ],
    },
    isLoading: false,
  }),
  useGetComponentListSpec: () => ({
    data: { data: [] },
    isLoading: false,
  }),
}));

vi.mock("@/stores/app.store", () => ({
  useAppStore: () => ({ currentProduct: "test-product" }),
}));

const defaultProps = {
  targetSpec: {
    metadata: {
      logo: "mock-logo.png",
    },
  },
  supportInfo: "UNI",
  apis: 3,
  title: "Test API",
  targetYaml: {
    info: {
      description: "**Title** Description of the API",
    },
  },
  item: {
    metadata: {
      key: "item-key",
      labels: { label1: "Label 1", label2: "Label 2" },
    },
  } as unknown as IUnifiedAsset,
  openDrawer: mockOpenDrawer,
};

describe("API Components - Tabs and Navigation", () => {
  it("renders all tabs correctly", async () => {
    render(
      <Router>
        <ApiComponents />
      </Router>
    );

    expect(screen.getByText("Standard API Mapping")).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: "Access Eline" })).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: "Internet Access" })).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: "Uni" })).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: "Shared" })).toBeInTheDocument();
  });

  it("switches tabs and renders the correct content", async () => {
    render(
      <Router>
        <ApiComponents />
      </Router>
    );
    const accessElineTab = screen.getByRole("tab", { name: "Access Eline" });
    const internetAccessTab = screen.getByRole("tab", { name: "Internet Access" });

    expect(accessElineTab).toHaveAttribute("aria-selected", "true");
    fireEvent.click(internetAccessTab);

    expect(internetAccessTab).toHaveAttribute("aria-selected", "true");
  });

  it("renders ApiComponent correctly inside a tab", () => {
    render(
      <Router>
        <ApiComponent {...defaultProps} />
      </Router>
    );

    expect(screen.getByText("Test API")).toBeInTheDocument();
    expect(screen.getByText("3 APIs")).toBeInTheDocument();
    expect(screen.getByText("Label 1")).toBeInTheDocument();
    expect(screen.getByText("Label 2")).toBeInTheDocument();
    expect(screen.getByText("Description of the API")).toBeInTheDocument();
  });

  it("navigates to the correct URL when clicked inside a tab", () => {
    render(
      <Router>
        <ApiComponent {...defaultProps} />
      </Router>
    );

    const apiComponent = screen.getByText("Test API").closest("div");
    fireEvent.click(apiComponent!);

    expect(mockNavigate).toHaveBeenCalledWith("/api-mapping/item-key", {
      state: { productType: "UNI" },
    });
  });
});
