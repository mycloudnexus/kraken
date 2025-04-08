import * as productHooks from "@/hooks/product";
import * as homepageHooks from "@/hooks/homepage";
import StandardAPIMappingTable from "@/pages/StandardAPIMappingTable";
import * as mappingStore from "@/stores/mappingUi.store";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { vi } from "vitest";

let mockedProductType = "UNI";

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useLocation: () => ({
      state: { productType: mockedProductType },
    }),
  };
});

test("StandardAPIMappingTable", () => {
  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    currentProduct: "test",
  });

  vi.spyOn(productHooks, "useGetComponentListAPI").mockReturnValue({
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
            {
              path: "/path2",
              method: "get",
              productTypes: ["UNI", "ACCESS_E_LINE"],
            },
          ],
        },
      },
    ],
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
            productType: "UNI",
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
          productType: "productType",
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
});

test("productTypeOptions should return correct options", async () => {
  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    currentProduct: "test",
  });

  vi.spyOn(homepageHooks, "useGetProductTypeList").mockReturnValue({
    data: ["UNI:UNI", "ACCESS_E_LINE:Access E Line"],
  } as any);

  vi.spyOn(productHooks, "useGetComponentListAPI").mockReturnValue({
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
            {
              path: "/path2",
              method: "get",
              productTypes: ["UNI", "ACCESS_E_LINE"],
            },
          ],
        },
      },
    ],
  } as any);

  render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMappingTable />
      </BrowserRouter>
    </QueryClientProvider>
  );

  const options = await screen.findAllByText(/UNI|Access E Line/);
  expect(options).toHaveLength(1);
  expect(options[0].textContent).toBe("UNI");
});

test("filteredComponentList should exclude SHARE if productType is not SHARE", () => {
  mockedProductType = "UNI";

  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    currentProduct: "test",
  });

  vi.spyOn(productHooks, "useGetComponentListAPI").mockReturnValue({
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
            }
          ],
        },
      },
      {
        kind: "kind",
        apiVersion: "v1",
        metadata: {
          name: "Address Validation API Management",
          key: "address_key",
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
            productType: "UNI",
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
          productType: "productType",
        },
      ],
    },
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

  // Test that SHARE component is excluded if productType is not "SHARE"
  const componentList = screen.queryAllByText("Product Offering Qualification (POQ) API Management");
  expect(componentList.length).toBe(1);  // Should only return the component not related to SHARE
});

test("filteredComponentList should include only SHARE components if productType is SHARE", () => {
  mockedProductType = "SHARE";

  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    currentProduct: "test",
  });

  vi.spyOn(productHooks, "useGetComponentListAPI").mockReturnValue({
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
    ],
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetail").mockReturnValue({
    data: {
      metadata: {
        name: "SHARE Component",
      },
    },
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetailMapping").mockReturnValue({
    data: {
      details: [],
    },
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

  // SHARE component should appear, non-SHARE should not
  expect(screen.queryByText("SHARE Component")).toBeInTheDocument();
  expect(screen.queryByText("Non-SHARE Component")).not.toBeInTheDocument();
});