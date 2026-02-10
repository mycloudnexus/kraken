import * as productHooks from "@/hooks/product";
import * as homepageHooks from "@/hooks/homepage";
import StandardAPIMappingTable from "@/pages/StandardAPIMappingTable";
import * as mappingStore from "@/stores/mappingUi.store";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { vi } from "vitest";

const mockUseLocation = vi.fn();
const mockNavigate = vi.fn();

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useLocation: () => mockUseLocation(),
    useNavigate: () => mockNavigate,
  };
});

vi.mock("@/pages/StandardAPIMapping/components/ComponentSelect", async () => {
  return {
    default: ({ componentList }: any) => {
      return (
        <div data-testid="mock-component-select">
          {(componentList?.data ?? []).map((c: any) => (
            <div key={c.metadata.key}>{c.metadata.name}</div>
          ))}
        </div>
      );
    },
  };
});

beforeEach(() => {
  vi.clearAllMocks();
  mockUseLocation.mockReturnValue({ state: { productType: "UNI" } });
});

test("StandardAPIMappingTable renders correctly", () => {
  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    currentProduct: "test",
  });

  vi.spyOn(homepageHooks, "useGetProductTypeList").mockReturnValue({
    data: ["UNI:UNI"],
  } as any);

  vi.spyOn(productHooks, "useGetComponentListAPI").mockReturnValue({
    data: {
      data: [
        {
          kind: "kind",
          apiVersion: "v1",
          metadata: {
            name: "Product Offering Qualification (POQ) API Management",
            key: "mock_key",
          },
          facets: {
            supportedProductTypesAndActions: [
              {
                path: "/path1",
                method: "post",
                actionTypes: ["add"],
                productTypes: ["UNI", "ACCESS_E_LINE"],
              },
            ],
          },
        },
      ],
    },
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetail").mockReturnValue({
    data: {
      metadata: {
        name: "Product Offering Qualification (POQ) API Management",
      },
    },
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetailMapping").mockReturnValue({
    data: { details: [] },
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any);

  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMappingTable />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("displays correct main title based on productType", async () => {
  mockUseLocation.mockReturnValue({ state: { productType: "ACCESS_E_LINE" } });

  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    currentProduct: "test",
  });

  vi.spyOn(homepageHooks, "useGetProductTypeList").mockReturnValue({
    data: ["UNI:UNI", "ACCESS_E_LINE:Access E Line"],
  } as any);

  vi.spyOn(productHooks, "useGetComponentListAPI").mockReturnValue({
    data: { data: [] },
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetail").mockReturnValue({ data: {} } as any);
  vi.spyOn(productHooks, "useGetComponentDetailMapping").mockReturnValue({ data: { details: [] } } as any);

  render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMappingTable />
      </BrowserRouter>
    </QueryClientProvider>
  );

  expect(await screen.findByText("Access E Line")).toBeInTheDocument();
  expect(screen.queryByText("UNI")).not.toBeInTheDocument();
});

test("filteredComponentList returns empty array if componentList is empty", () => {
  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    currentProduct: "test",
  });

  vi.spyOn(homepageHooks, "useGetProductTypeList").mockReturnValue({ data: [] } as any);

  vi.spyOn(productHooks, "useGetComponentListAPI").mockReturnValue({
    data: { data: [] }
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetail").mockReturnValue({ data: {} } as any);
  vi.spyOn(productHooks, "useGetComponentDetailMapping").mockReturnValue({
    data: { details: [] },
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any);

  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMappingTable />
      </BrowserRouter>
    </QueryClientProvider>
  );

  expect(container.innerHTML).not.toContain("SHARE Component");
  expect(container.innerHTML).not.toContain("Non-SHARE Component");
});

test("filteredComponentList should exclude SHARE if productType is not SHARE", async () => {
  mockUseLocation.mockReturnValue({ state: { productType: "UNI" } });

  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    currentProduct: "test",
  });

  vi.spyOn(homepageHooks, "useGetProductTypeList").mockReturnValue({ data: [] } as any);

  vi.spyOn(productHooks, "useGetComponentListAPI").mockReturnValue({
    data: {
      data: [
        {
          kind: "kraken.component.api",
          apiVersion: "v1",
          metadata: {
            name: "Product Offering Qualification (POQ) API Management",
            key: "mef.sonata.api.poq",
          },
          facets: {
            supportedProductTypesAndActions: [
              {
                path: "/mefApi/sonata/productOfferingQualification/v7/productOfferingQualification",
                method: "post",
                productTypes: ["UNI"],
              },
            ],
          },
        },
        {
          kind: "kraken.component.api",
          apiVersion: "v1",
          metadata: {
            name: "Address Validation API Management",
            key: "mef.sonata.api.serviceability.address",
          },
          facets: {
            supportedProductTypesAndActions: [
              {
                path: "/path2",
                method: "get",
                productTypes: ["SHARE"],
              },
            ],
          },
        },
      ],
    },
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetail").mockReturnValue({
    data: {
      metadata: { name: "Product Offering Qualification (POQ) API Management" },
    },
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetailMapping").mockReturnValue({
    data: { details: [] },
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any);

  render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMappingTable />
      </BrowserRouter>
    </QueryClientProvider>
  );

  expect(await screen.findByText("Product Offering Qualification (POQ) API Management")).toBeInTheDocument();

  expect(screen.queryByText("Address Validation API Management")).not.toBeInTheDocument();

  const componentList = screen.queryAllByText("Product Offering Qualification (POQ) API Management");
  expect(componentList.length).toBe(1);
});

test("filteredComponentList should include only SHARE components if productType is SHARE", async () => {
  mockUseLocation.mockReturnValue({ state: { productType: "SHARE" } });

  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    currentProduct: "test",
  });

  vi.spyOn(homepageHooks, "useGetProductTypeList").mockReturnValue({ data: [] } as any);

  vi.spyOn(productHooks, "useGetComponentListAPI").mockReturnValue({
    data: {
      data: [
        {
          kind: "kind",
          apiVersion: "v1",
          metadata: {
            name: "SHARE Component",
            key: "share_key",
          },
          facets: {
            supportedProductTypesAndActions: [
              {
                path: "/share",
                method: "get",
                productTypes: ["SHARE"],
              },
            ],
          },
        },
        {
          kind: "kind",
          apiVersion: "v1",
          metadata: {
            name: "Non-SHARE Component",
            key: "non_share_key",
          },
          facets: {
            supportedProductTypesAndActions: [
              {
                path: "/not-share",
                method: "get",
                productTypes: ["UNI"],
              },
            ],
          },
        },
      ]
    }
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetail").mockReturnValue({
    data: { metadata: { name: "SHARE Component" } },
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetailMapping").mockReturnValue({
    data: { details: [] },
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any);

  render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMappingTable />
      </BrowserRouter>
    </QueryClientProvider>
  );

  expect(await screen.findByText("SHARE Component")).toBeInTheDocument();
  expect(screen.queryByText("Non-SHARE Component")).not.toBeInTheDocument();
});

test("clicking Mapping button navigates with correct state", async () => {
  mockUseLocation.mockReturnValue({ state: { productType: "UNI" } });

  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    currentProduct: "test",
  });

  vi.spyOn(homepageHooks, "useGetProductTypeList").mockReturnValue({ data: [] } as any);

  vi.spyOn(productHooks, "useGetComponentListAPI").mockReturnValue({
    data: {
      data: [
        {
          kind: "kind",
          apiVersion: "v1",
          metadata: {
            name: "Product Offering Qualification (POQ) API Management",
            key: "mock_key",
          },
          facets: {
            supportedProductTypesAndActions: [
              {
                path: "/a/b/c/d/e",
                method: "GET",
                actionTypes: ["add"],
                productTypes: ["UNI"],
              },
            ],
          },
        },
      ],
    }
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetail").mockReturnValue({
    data: {
      metadata: {
        name: "Product Offering Qualification (POQ) API Management",
      },
    },
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetailMapping").mockReturnValue({
    data: {
      details: [
        {
          description: "mock_mapping",
          mappingMatrix: {
            productType: "uni",
          },
          mappingStatus: "in progress",
          method: "GET",
          orderBy: "createdAt",
          path: "/a/b/c/d/e",
          requiredMapping: false,
          targetKey: "targetKey",
          targetMapperKey: "targetMapperKey",
          updatedAt: "2024-12-3T01:22:00Z",
          actionType: "actionType",
          diffWithStage: false,
          lastDeployedAt: "2024-12-3T01:22:00Z",
          order: 1,
          productType: "uni",
        },
      ],
    },
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any);

  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMappingTable />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
  const mappingButton = await screen.findByText("Mapping");
  mappingButton.click();

  expect(mockNavigate).toHaveBeenCalledWith("targetKey", {
    state: expect.objectContaining({
      mainTitle: expect.any(String),
      filteredComponentList: expect.any(Array),
      productType: "UNI",
    }),
  });
});