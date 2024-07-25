import { fireEvent, render, renderHook, waitFor } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import StandardAPIMapping from "@/pages/StandardAPIMapping";
import { useMappingUiStore } from '@/stores/mappingUi.store';
import * as productHooks from '@/hooks/product';

test("StandardAPIMapping page", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMapping />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("StandardAPIMapping btn-create-version", () => {
  vi.mock("@/hooks/product", async () => {
    const actual = await vi.importActual("@/hooks/product");
    return {
      ...actual,
      useCreateNewVersion: vi.fn().mockResolvedValue({
        mutateAsync: vi
          .fn()
          .mockRejectedValue(new Error({ reason: "abc" } as any)),
        isLoading: false,
      }),
    };
  });
  const { getByTestId } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMapping />
      </BrowserRouter>
    </QueryClientProvider>
  );
  const btn = getByTestId("btn-create-version");
  expect(btn).toBeInTheDocument();
  fireEvent.click(btn);
  waitFor(() => {
    const btnOk = getByTestId("btn-ok");
    expect(btnOk).toBeInTheDocument();
    fireEvent.click(btnOk);
  });
});

test("StandardAPIMapping page with different UI state", () => {
  const { result: uiResult } = renderHook(() => useMappingUiStore());
  uiResult.current.setMappingInProgress(true);

  vi.spyOn(productHooks, "useGetComponentDetailMapping").mockReturnValue({
    data: {
      details: [
        {
          "targetKey": "mef.sonata.api-target.quote.eline.add",
          "targetMapperKey": "mef.sonata.api-target-mapper.quote.eline.add",
          "description": "This operation creates a Quote entity",
          "path": "/mefApi/sonata/quoteManagement/v8/quote",
          "method": "post",
          "mappingStatus": "incomplete",
          "updatedAt": "2024-07-24T08:22:09.299308Z",
          "diffWithStage": true,
          "productType": "access_e_line",
          "actionType": "add",
          "mappingMatrix": {
            "quoteLevel": "firm",
            "syncMode": false,
            "productType": "access_e_line",
            "actionType": "add"
          }
        },
        {
          "targetKey": "mef.sonata.api-target.quote.uni.read",
          "targetMapperKey": "mef.sonata.api-target-mapper.quote.uni.read",
          "description": "This operation retrieves a Quote entity. Attribute selection is enabled for all first level attributes.",
          "path": "/mefApi/sonata/quoteManagement/v8/quote/{id}",
          "method": "get",
          "mappingStatus": "incomplete",
          "updatedAt": "2024-07-24T08:22:09.780463Z",
          "diffWithStage": true,
          "productType": "uni",
          "mappingMatrix": {
            "quoteLevel": "firm",
            "syncMode": false,
            "productType": "uni"
          }
        },
        {
          "targetKey": "mef.sonata.api-target.quote.eline.read",
          "targetMapperKey": "mef.sonata.api-target-mapper.quote.eline.read",
          "description": "This operation retrieves a Quote entity. Attribute selection is enabled for all first level attributes.",
          "path": "/mefApi/sonata/quoteManagement/v8/quote/{id}",
          "method": "get",
          "mappingStatus": "incomplete",
          "updatedAt": "2024-07-24T08:22:09.898981Z",
          "diffWithStage": true,
          "productType": "access_e_line",
          "mappingMatrix": {
            "quoteLevel": "firm",
            "syncMode": false,
            "productType": "access_e_line"
          }
        },
        {
          "targetKey": "mef.sonata.api-target.quote.uni.add",
          "targetMapperKey": "mef.sonata.api-target-mapper.quote.uni.add",
          "description": "This operation creates a Quote entity",
          "path": "/mefApi/sonata/quoteManagement/v8/quote",
          "method": "post",
          "mappingStatus": "incomplete",
          "updatedAt": "2024-07-24T08:22:09.588098Z",
          "diffWithStage": true,
          "productType": "uni",
          "actionType": "add",
          "mappingMatrix": {
            "quoteLevel": "firm",
            "syncMode": false,
            "productType": "uni",
            "actionType": "add"
          }
        }
      ]
    },
    isLoading: false,
  } as any);

  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMapping />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
