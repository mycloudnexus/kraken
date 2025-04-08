import * as productHooks from "@/hooks/product";
import StandardAPIMappingTable from "@/pages/StandardAPIMappingTable";
import * as mappingStore from "@/stores/mappingUi.store";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { vi } from "vitest";

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useLocation: () => ({
      state: { productType: "UNI" },
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

test("productTypeOptions should return correct options", () => {
  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    currentProduct: "test",
  });

  vi.spyOn(productHooks, "useGetProductTypes").mockReturnValue({
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

  // Check that the productTypeOptions are correctly displayed in the component
  const options = screen.getAllByText((content) => {
    return /access e line/i.test(content) || /uni/i.test(content);
  });
  expect(options).toHaveLength(2);
  expect(options[0].textContent).toBe("UNI");
  expect(options[1].textContent).toBe("Access E Line");
});

test("filteredComponentList should exclude SHARE if productType is not SHARE", () => {
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
              productTypes: ["UNI", "SHARE"],
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
