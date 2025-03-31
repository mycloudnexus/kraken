import ApiComponent from "@/pages/HomePage/components/ApiComponent";
import ApiComponents from "@/pages/HomePage/components/ApiComponents";
import { StandardApiComponent } from "@/utils/types/product.type";
import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter as Router } from "react-router-dom";
import { vi } from "vitest";

const mockOpenDrawer = vi.fn();
const mockNavigate = vi.fn();

vi.mock("react-router-dom", () => ({
  useNavigate: () => mockNavigate,
  BrowserRouter: ({ children }: { children: React.ReactNode }) => (
    <div>{children}</div>
  ),
}));

vi.mock("@/hooks/homepage", () => ({
  useGetProductTypeList: () => ({
    data: [
      "ACCESS_E_LINE:Access Eline",
      "INTERNET_ACCESS:Internet Access",
      "UNI:Uni",
      "SHARED:Shared",
    ],
  }),
  useGetStandardApiComponents: (selectedProductType: string) => ({
    data:
      selectedProductType === "UNI"
        ? [
            {
              name: "Test API",
              componentKey: "item-key",
              apiCount: 3,
              baseSpec: { content: "mock-content" },
              supportedProductTypes: ["UNI"],
            },
          ]
        : [],
    isLoading: false,
  }),
}));

vi.mock("@/stores/app.store", () => ({
  useAppStore: () => ({ currentProduct: "test-product" }),
}));

const defaultProps = {
  supportInfo: "UNI",
  apis: 3,
  title: "Test API",
  logo: "mock-logo.png",
  targetYaml: {
    info: {
      description: "**Title** Description of the API",
    },
  },
  item: {
    name: "Test API",
    componentKey: "item-key",
    supportedProductTypes: ["UNI"],
    labels: { label1: "Label 1", label2: "Label 2" },
    logo: "mock-logo.png",
    baseSpec: {
      path: "/api-path",
      content: "mock-content",
      format: "json",
    },
    apiCount: 3,
  } as StandardApiComponent,
  openDrawer: mockOpenDrawer,
};

describe("API Components - Tabs and Navigation", () => {
  it("renders all tabs correctly", () => {
    render(
      <Router>
        <ApiComponents />
      </Router>
    );

    expect(screen.getByText("Standard API Mapping")).toBeInTheDocument();
    expect(
      screen.getByRole("tab", { name: "Access Eline" })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("tab", { name: "Internet Access" })
    ).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: "Uni" })).toBeInTheDocument();
    expect(screen.getByRole("tab", { name: "Shared" })).toBeInTheDocument();
  });

  it("switches tabs and renders the correct content", () => {
    render(
      <Router>
        <ApiComponents />
      </Router>
    );
    const accessElineTab = screen.getByRole("tab", { name: "Access Eline" });

    expect(screen.getByRole("tab", { name: "Uni" })).toBeInTheDocument();
    fireEvent.click(accessElineTab);

    expect(
      screen.getByRole("tab", { name: "Access Eline" })
    ).toBeInTheDocument();
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
