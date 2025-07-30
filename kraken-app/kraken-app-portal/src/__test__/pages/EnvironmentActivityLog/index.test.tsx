import {fireEvent, render, screen} from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import EnvironmentActivityLog from "@/pages/EnvironmentActivityLog";
import * as envHooks from '@/hooks/product'
import * as buyerList from "@/services/products.ts";

test("EnvironmentActivityLog page", () => {
  vi.spyOn(envHooks, 'useGetProductEnvs').mockReturnValue({
    data: {
      data: [
        {
          id: '1',
          name: 'dev',
          createdAt: '11111',
          productId: '1',
        }
      ],
      total: 1,
      size: 10,
      page: 0,
    },
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any)

  vi.spyOn(envHooks, 'useGetProductTypes').mockReturnValue({
          data: [
              "UNI:UNI",
              "ACCESS_E_LINE:Access Eline",
              "SHARE:Shared"
          ]
      } as any)

  vi.spyOn(envHooks, 'useGetProductEnvActivities').mockReturnValue({
    data: {
      data: [
        {
          env: "test",
          requestId: "test",
          uri: "test",
          path: "test",
          method: "test",
          buyerName: "test",
          queryParameters: {},
          headers: {},
          request: {},
          response: {},
          createdAt: "test",
          updatedAt: "test",
          httpStatusCode: 200,
          requestIp: "test",
          responseIp: "test",
          callSeq: 20,
        }
      ],
      total: 1,
      size: 10,
      page: 0,
    },
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any)

  vi.spyOn(buyerList, 'getBuyerList').mockResolvedValue({
    "code": 200,
    "message": "OK",
    "data": {
      "data": [
        {
          "kind": "kraken.product-buyer",
          "apiVersion": "v1",
          "facets": {
            "buyerInfo": {
              "envId": "b2d775e5-44ad-43cb-8dd4-6fbe52585ec9",
              "buyerId": "test-store",
              "companyName": "test-store"
            }
          },
          "links": [],
          "id": "2ba5cf86-bde1-42e8-8cb5-50e07444eee6",
        }
      ],
      "total": 1,
      "page": 0,
      "size": 30
    }
  })


  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <EnvironmentActivityLog />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
  const selectInput = screen.getByTitle('select-buyer');
  fireEvent.change(selectInput, {target: {value: 'test'}})
});
