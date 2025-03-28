import * as productModule from "@/hooks/product";
import Buyer from "@/pages/Buyer";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

test("Buyer page", () => {
  vi.spyOn(productModule, "useGetBuyerList").mockReturnValue({
    data: {
      data: [
        {
          kind: "kraken.product-buyer",
          apiVersion: "v1",
          metadata: {
            id: "metadata-id-1",
            name: "Buyer",
            version: 0,
            key: "test-key1",
            description: "",
            status: "deactivated",
            labels: {
              issueAt: "issueAt",
              envId: "envId",
              buyerId: "buyerId",
            },
          },
          facets: {
            buyerInfo: {
              envId: "envId",
              buyerId: "buyerId",
              companyName: "companyName",
            },
          },
          links: [],
          id: "test-id-1",
          parentId: "parentId",
          createdAt: "createdAt",
          createdBy: "createdBy",
          updatedAt: "updatedAt",
          syncMetadata: {
            fullPath: "",
            syncedSha: "",
            syncedAt: "syncedAt",
            syncedBy: "syncedBy",
          },
        },
        {
          kind: "kraken.product-buyer",
          apiVersion: "v1",
          metadata: {
            id: "metadata-id-2",
            name: "Buyer",
            version: 0,
            key: "test-key2",
            description: "",
            status: "activated",
            labels: {
              issueAt: "issueAt",
              envId: "envId",
              buyerId: "buyerId",
            },
          },
          facets: {
            buyerInfo: {
              envId: "envId",
              buyerId: "buyerId",
              companyName: "companyName",
            },
          },
          links: [],
          id: "test-id-2",
          parentId: "parentId",
          createdAt: "createdAt",
          createdBy: "createdBy",
          updatedAt: "updatedAt",
          syncMetadata: {
            fullPath: "",
            syncedSha: "",
            syncedAt: "syncedAt",
            syncedBy: "syncedBy",
          },
        },
      ],
      page: 0,
      total: 2,
      size: 10,
    },
    isLoading: false,
  } as any);
  vi.spyOn(productModule, "useActiveBuyer").mockReturnValue({
    mutateAsync: () => {
      return {
        data: {
          buyerToken: {
            accessToken: "test-token",
          },
        },
      };
    },
  } as any);
  const { container, getByTestId } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Buyer />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
  const activateBuyer = getByTestId("test-id-1-activate");
  fireEvent.click(activateBuyer);
});
