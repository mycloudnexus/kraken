import * as productHooks from "@/hooks/product";
import StandardAPIMappingTable from "@/pages/StandardAPIMappingTable";
import * as mappingStore from "@/stores/mappingUi.store";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
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
