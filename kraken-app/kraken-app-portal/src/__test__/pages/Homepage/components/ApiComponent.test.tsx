import ApiComponent from "@/pages/HomePage/components/ApiComponent";
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

describe("ApiComponent", () => {
  it("renders ApiComponent correctly", () => {
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

  it("navigates to the correct URL when clicked", () => {
    render(
      <Router>
        <ApiComponent {...defaultProps} />
      </Router>
    );

    const apiComponent = screen.getByText("Test API").closest("div");
    fireEvent.click(apiComponent!);

    fireEvent.click(screen.getByText("Test API"));
    expect(mockNavigate).toHaveBeenCalledWith("/api-mapping/item-key", {
      state: { productType: "UNI" },
    });
  });
});
