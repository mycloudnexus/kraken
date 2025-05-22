import * as productHooks from "@/hooks/product";
import StandardAPIMapping from "@/pages/StandardAPIMapping";
import * as mappingStore from "@/stores/mappingUi.store";
import * as newApiMappingStore from "@/stores/newApiMapping.store";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

// 4. Create mock function
const mockUpdateTargetMapper = vi
  .fn()
  .mockResolvedValue({ message: "success" });

test("StandardAPIMapping handleSave updates and sends correct mapping", async () => {
  // 1. Mock the app store
  vi.mock("@/stores/app.store", () => ({
    useAppStore: vi.fn(() => ({
      currentProduct: "test-product",
      setCurrentProduct: vi.fn(),
    })),
  }));

  // 3. Mock useGetApiSpec
  vi.mock("@/pages/NewAPIMapping/components/useGetApiSpec", () => ({
    __esModule: true,
    default: () => ({
      mapperResponse: {
        kind: "Component",
        apiVersion: "v1",
        id: "mock-component-id",
        parentId: "mock-parent-id",
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        updatedBy: "test-user",
        syncMetadata: {
          fullPath: "/mock/full/path",
          syncedSha: "mock-sha-123",
          syncedAt: new Date().toISOString(),
          syncedBy: "test-user",
        },
        metadata: {
          id: "mapper-id",
          name: "Mock Component",
          version: 1,
          key: "mock-key",
          description: "This is a mock component used for testing.",
          labels: {
            deployedStatus: "deployed", // example values
            stageDeployedStatus: "staged",
            subVersion: "1.0.0",
            version: "1",
            componentKey: "mock-key",
          },
        },
        facets: {
          endpoints: [
            {
              id: "mock-endpoint-id",
              path: "/mock-path",
              method: "GET",
              serverKey: "mock-server-key",
              mappers: {
                request: [
                  {
                    id: "mock-duration-unit-id",
                    description: "Duration unit",
                    name: "mapper.order.uni.add.duration.units",
                    title: "order item Term unit",
                    source:
                      "@{{productOrderItem[0].requestedItemTerm.duration.units}}",
                    target: "@{{durationUnit}}",
                    sourceType: "enum",
                    sourceValues: [
                      "calendarYears",
                      "calendarMonths",
                      "calendarDays",
                      "calendarHours",
                      "calendarMinutes",
                      "businessDays",
                      "businessHours",
                      "businessMinutes",
                    ],
                    valueMapping: {
                      calendarDays: "d",
                      calendarYears: "y",
                      calendarMonths: "m",
                    },
                    sourceLocation: "BODY",
                    targetLocation: "BODY",
                    allowValueLimit: false,
                    customizedField: false,
                    requiredMapping: true,
                  },
                ],
                response: [
                  {
                    id: "mock-uni-state-id",
                    description: "uni state",
                    name: "mapper.order.uni.add.state",
                    title: "Order State",
                    source: "@{{status}}",
                    target: "@{{state}}",
                    targetType: "enum",
                    targetValues: [
                      "acknowledged",
                      "assessingCancellation",
                      "held.assessingCharge",
                      "pending.assessingModification",
                      "cancelled",
                      "pendingCancellation",
                      "completed",
                      "failed",
                      "inProgress",
                      "partial",
                      "rejected",
                    ],
                    valueMapping: {
                      failed: "failed",
                    },
                    sourceLocation: "BODY",
                    targetLocation: "BODY",
                    allowValueLimit: false,
                    customizedField: false,
                    requiredMapping: true,
                  },
                ],
              },
            },
          ],
          trigger: {
            path: "/mock-trigger-path",
            method: "POST",
            addressType: "internal",
            provideAlternative: false,
          },
        },
        links: [],
      },
      serverKeyInfo: {
        method: "GET",
        path: "/mock-path",
        serverKey: "mock-server-key",
      },
      mappers: {
        request: [],
        response: [],
      },
      metadataKey: "mock-metadata-key",
      resetMapping: vi.fn(),
      refreshMappingDetail: vi.fn(),
      jsonSpec: {},
      loadingMapper: false,
      resetResponseMapping: vi.fn(),
    }),
  }));

  // 5. Mock the actual hook the component uses
  vi.mock("@/hooks/product", async () => {
    const actual = await vi.importActual<typeof import("@/hooks/product")>("@/hooks/product");
    return {
      ...actual,
      useUpdateTargetMapper: () => ({
        mutateAsync: mockUpdateTargetMapper,
        isPending: false,
      }),
    };
  });

  // 6. Mock other stores
  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    activePath: "/a/b/c/d/e",
    setActivePath: vi.fn(),
    selectedKey: "targetKey",
    setSelectedKey: vi.fn(),
  });

  vi.spyOn(productHooks, "useGetComponentDetailMapping").mockReturnValue({
    data: {
      details: [
        {
          path: "/a/b/c/d/e",
          requiredMapping: true,
          targetKey: "targetKey",
          targetMapperKey: "targetMapperKey",
          updatedAt: "2024-12-3T01:22:00Z",
          orderBy: "createdAt",
        },
      ],
    },
    refetch: vi.fn(),
    isLoading: false,
  } as any);

  vi.spyOn(productHooks, "useGetComponentDetail").mockReturnValue({
    data: {
      metadata: {
        name: "Test API",
        componentKey: "item-key",
      },
    },
    isLoading: false,
  } as any);

  // 7. Mock newApiMappingStore
  const newApiMappingStoreMock = {
    currentProduct: "test-product",
    query: JSON.stringify({
      targetMapperKey: "targetMapperKey",
      updatedAt: "2024-12-3T01:22:00Z",
    }),
    sellerApi: { method: "GET", url: "/api/test" },
    serverKey: "mock-server-key",
    requestMapping: [
      {
        name: "request-mapping-fieldGroup1",
        source: "source1",
        target: "target1",
        targetLocation: "BODY",
        sourceLocation: "BODY",
        requiredMapping: true,
        valueMapping: { originField: "mappedField" },
      },
      {
        name: "mapper.order.uni.add.duration.units",
        title: "order item Term unit",
        source: "@{{productOrderItem[0].requestedItemTerm.duration.units}}",
        target: "@{{durationUnit}}",
        sourceType: "enum",
        sourceValues: [
          "calendarYears",
          "calendarMonths",
          "calendarDays",
          "calendarHours",
          "calendarMinutes",
          "businessDays",
          "businessHours",
          "businessMinutes"
        ],
        valueMapping: {
          "calendarYears": "y",
          "calendarMonths": "m"
        },
        sourceLocation: "BODY",
        targetLocation: "BODY",
        allowValueLimit: false,
        customizedField: false,
        requiredMapping: true,
        id: "uwH5h5MtdyTwUYIAPc1Zb"
      },
    ],
    responseMapping: [
      {
        name: "response-mapping-fieldGroup1",
        source: "source2",
        target: "target2",
        targetLocation: "BODY",
        sourceLocation: "BODY",
        requiredMapping: true,
        valueMapping: { mappedField: "originField" },
      },
      {
        name: "mapper.order.uni.add.state",
        title: "Order State",
        source: "@{{status}}",
        target: "@{{state}}",
        targetType: "enum",
        description: "",
        targetValues: [
          "acknowledged",
          "assessingCancellation",
          "held.assessingCharge",
          "pending.assessingModification",
          "cancelled",
          "pendingCancellation",
          "completed",
          "failed",
          "inProgress",
          "partial",
          "rejected"
        ],
        valueMapping: {
          "failed": "failed"
        },
        sourceLocation: "BODY",
        targetLocation: "BODY",
        allowValueLimit: false,
        customizedField: false,
        requiredMapping: true,
        id: "8v-IOJDaH6JSob1eV1u7Y"
      },
    ],
    listMappingStateRequest: [
      {
        name: "state-request-fieldGroup1",
        from: "originField",
        to: ["mappedField"],
        key: "key-1",
      },
      {
        "name": "mapper.order.uni.add.duration.units",
        "key": "GK5U5fd4wS46GwwtGil10",
        "from": "calendarYears",
        "to": [
          "y"
        ]
      },
      {
        "name": "mapper.order.uni.add.duration.units",
        "key": "5FEsD7sefMvcotR72Gl-r",
        "from": "calendarMonths",
        "to": [
          "m"
        ]
      }
    ],
    listMappingStateResponse: [
      {
        name: "state-response-fieldGroup1",
        from: "mappedField",
        to: ["originField"],
        key: "key-2",
      },
      {
        "from": "failed",
        "to": [
          "failed"
        ],
        "key": "1jpxSBSD7JnNe_laTgFzt",
        "name": "mapper.order.uni.add.state"
      }
    ],
    setRequestMapping: vi.fn(),
    setResponseMapping: vi.fn(),
    setSellerApi: vi.fn(),
    setListMappingStateRequest: vi.fn,
    setListMappingStateResponse: vi.fn(),
    reset: vi.fn(),
    setQuery: vi.fn(),
  };
  vi.spyOn(newApiMappingStore, "useNewApiMappingStore").mockReturnValue(
    newApiMappingStoreMock
  );

  // 8. Render component
  const { getByTestId } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMapping />
      </BrowserRouter>
    </QueryClientProvider>
  );

  // 9. Trigger save
  const saveButton = getByTestId("btn-save");
  fireEvent.click(saveButton);

  // 10. Verify the update was called
  await waitFor(() => {
    expect(mockUpdateTargetMapper).toHaveBeenCalled();
  });

  // 11. Verify the call arguments
  const callArgs = mockUpdateTargetMapper.mock.calls[0][0];
  expect(callArgs).toEqual({
    productId: "test-product",
    componentId: "mapper-id",
    data: expect.objectContaining({
      metadata: expect.any(Object),
      facets: expect.objectContaining({
        endpoints: expect.arrayContaining([
          expect.objectContaining({
            mappers: expect.any(Object),
            method: "GET",
            path: "/api/test",
            serverKey: "mock-server-key",
          }),
        ]),
      }),
    }),
  });
});