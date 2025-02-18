import * as sellerAPIModule from "@/hooks/product";
import APIServerList from "@/pages/APIServerList";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

const getSellerContactsResponse = {
  data: [
    {
      kind: "kraken.component.seller-contact",
      apiVersion: "v1",
      metadata: {
        id: "c7367e50-25e7-4a0a-86cc-66c8688b2a85",
        name: "mef.sonata.seller.contact",
        version: 9,
        key: "mef.sonata.api.quote.access.eline",
        description: "seller contact information",
        status: "activated",
        labels: {
          "access.eline": "true",
          componentKey: "mef.sonata.api.quote",
        },
      },
      facets: {
        sellerInfo: {
          name: "cheng",
          role: "sellerContactInformation",
          number: "111111",
          emailAddress: "cheng@consoleconnect.cn",
        },
      },
      links: [],
      id: "c7367e50-25e7-4a0a-86cc-66c8688b2a85",
      parentId: "a5205123-4b89-4877-b0db-774584b7571a",
      createdAt: "2025-01-08T05:30:22.270235Z",
      createdBy: "be6c884d-ca20-480a-9f15-a202a945b358",
      updatedAt: "2025-01-28T08:09:48.264033Z",
      updatedBy: "ed6e9f37-f3b8-4d55-8895-33cf3cae9b28",
      syncMetadata: {
        fullPath: "",
        syncedSha: "",
        syncedAt: "2025-01-28T08:09:48.252938337Z",
        syncedBy: "ed6e9f37-f3b8-4d55-8895-33cf3cae9b28",
      },
    },
    {
      kind: "kraken.component.seller-contact",
      apiVersion: "v1",
      metadata: {
        id: "725c13c3-4e9c-4ca8-84ec-4e8b30f03e58",
        name: "mef.sonata.seller.contact",
        version: 2,
        key: "mef.sonata.api.order.access.eline",
        description: "seller contact information",
        status: "activated",
        labels: {
          "access.eline": "true",
          componentKey: "mef.sonata.api.order",
        },
      },
      facets: {
        sellerInfo: {
          name: "chengOrder",
          role: "sellerContact",
          number: "22222",
          emailAddress: "chengOrder@consoleconnect.cn",
        },
      },
      links: [],
      id: "725c13c3-4e9c-4ca8-84ec-4e8b30f03e58",
      parentId: "a5205123-4b89-4877-b0db-774584b7571a",
      createdAt: "2025-01-08T05:30:05.324717Z",
      createdBy: "be6c884d-ca20-480a-9f15-a202a945b358",
      updatedAt: "2025-01-09T09:09:34.314004Z",
      updatedBy: "24044233-3683-425e-9cbe-62e643f081d8",
      syncMetadata: {
        fullPath: "",
        syncedSha: "",
        syncedAt: "2025-01-09T09:09:34.303970747Z",
        syncedBy: "24044233-3683-425e-9cbe-62e643f081d8",
      },
    },
  ],
  total: 2,
  page: 0,
  size: 500,
};

describe("test API Server List tab", () => {
  test("test API server setup page", () => {
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <APIServerList />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
  });

  test("test Contact information setup tab", () => {
    vi.spyOn(sellerAPIModule, "useGetSellerContacts").mockReturnValue({
      data: getSellerContactsResponse,
      refetch: () =>
        Promise.resolve({
          data: getSellerContactsResponse,
        }),
    } as any);
    const { container, getByText } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <APIServerList />
        </BrowserRouter>
      </QueryClientProvider>
    );

    const item = getByText("Contact information setup");
    fireEvent.click(item);
    expect(container).toBeInTheDocument();
  });
});
