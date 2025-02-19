import RequestItem from "@/pages/NewAPIMapping/components/RequestItem";
import RightAddSellerProp from "@/pages/NewAPIMapping/components/RightAddSellerProp";
import { useCommonAddProp } from "@/pages/NewAPIMapping/components/commonHook";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { queryClient } from "@/utils/helpers/reactQuery";
import { EnumRightType } from "@/utils/types/common.type";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, renderHook, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

beforeEach(() => {
  const mockFn = vi.fn();
  renderHook(() =>
    useCommonAddProp({
      pathParameters: [
        { name: "a", schema: { type: "object" } },
        { name: "a1", schema: { type: "string" } },
      ],
      onSelect: mockFn,
      setSelectedProp: mockFn,
      selectedProp: { location: "PATH", name: "a" },
      rightSideInfo: {
        method: "update",
        previousData: {
          name: "mapper.order.uni.delete.buyerId",
          title:
            "The unique identifier of the organization that is acting as the a Buyer.",
          source: "@{{query.buyerId}}",
          target: "",
          description: "",
          sourceLocation: "QUERY",
          targetLocation: "",
          requiredMapping: false,
        },
        title:
          "The unique identifier of the organization that is acting as the a Buyer.",
      },
      queryParameters: [
        { name: "b", schema: { type: "object" } },
        { name: "b1", schema: { type: "string" } },
      ],
      requestBodyTree: undefined,
    })
  );
  const { result } = renderHook(() => useNewApiMappingStore());
  result.current?.setRightSideInfo({
    method: "update",
    title: "The country that the address is in",
    previousData: {
      name: "address.validation.country",
      title: "The country that the address is in",
      source: "@{{submittedGeographicAddress.country}}",
      target:
        'filter={"where":{"company.addresses.country":"@{{submittedGeographicAddress.country}}","company.addresses.city":"@{{submittedGeographicAddress.city}}"},"skip":0,"limit":10}',
      description: "",
      sourceLocation: "BODY",
      targetLocation: "",
      requiredMapping: true,
    },
  });
  result.current.setRightSide(EnumRightType.AddSellerProp);
  result.current.setSellerApi({
    name: "console connect application",
    url: "/v2/data-center-facility",
    method: "get",
    spec: {
      summary: "Search DCFs",
      tags: ["Data Center Facilities (DCFs)"],
      description:
        "Search for available DCFs on and off the network.\n\nTo ensure a DCF is ready for physical port orders, ensure the property `dataCenterFacility.ready: true` is set when searching for DCFs. \nTo find a DCF that supports Hybrid NNI Ports, search for `dataCenterFacility.nniReady: true`.\n\nSee the Query Parameters section below for search examples.",
      parameters: [
        {
          description: "Criteria for searching DCFs.",
          in: "path",
          name: "filterPath",
          required: true,
          schema: {
            type: "object",
            properties: {
              where: {
                type: "object",
                description: "The criteria of properties to search for.",
                example: {
                  "company.addresses.city": "Sydney",
                },
              },
              skip: {
                type: "integer",
                description: 'The "page" of results from a query. accepted',
                example: 5,
              },
              limit: {
                type: "integer",
                description:
                  "The number of results to be returned in a query. This should be a small value for quicker load times.",
                example: 10,
              },
            },
            $$ref:
              "http://localhost:5173/api-mapping/mef.sonata.api.serviceability.address#/components/schemas/LoopBackFilter",
          },
          examples: {
            searchBySpeeds: {
              summary: "Search DCFs by Speed.",
              value:
                '{\n   "where": {\n      "dataCenterFacility.speeds.value": 10000,\n      "dataCenterFacility.ready": true\n   },\n   "skip": 0,\n   "limit": 10\n}',
            },
            searchByLocation: {
              summary: "Search DCFs by location.",
              value:
                '{\n   "where": {\n      "company.addresses.country": "AU",\n      "company.addresses.city": "Sydney"\n      "dataCenterFacility.speeds.value": 10000,\n      "dataCenterFacility.ready": true\n   },\n   "skip": 0,\n   "limit": 10\n}',
            },
            searchByName: {
              summary: "Search DCFs by location.",
              value:
                '{\n   "where": {\n      "name": { "like": "DCF Name", options: "i" },\n   },\n   "skip": 0,\n   "limit": 10\n}',
            },
            searchByCrossConnectAddOn: {
              summary: "Search DCFs by CrossConnect Add On.",
              value:
                '{\n   "where": {\n      "dataCenterFacility.canBundleCrossConnect": true,\n   },\n   "skip": 0,\n   "limit": 10\n}',
            },
            searchByFreeCrossConnect: {
              summary: "Search DCFs by Free CrossConnect.",
              value:
                '{\n   "where": {\n      "tags": {"in": "free-cross-connect"}\n   },\n   "skip": 0,\n   "limit": 10\n}',
            },
          },
        },
        {
          description: "Criteria for searching DCFs.",
          in: "query",
          name: "filter",
          required: true,
          schema: {
            type: "object",
            properties: {
              where: {
                type: "object",
                description: "The criteria of properties to search for.",
                example: {
                  "company.addresses.city": "Sydney",
                },
              },
              skip: {
                type: "integer",
                description: 'The "page" of results from a query. accepted',
                example: 5,
              },
              limit: {
                type: "integer",
                description:
                  "The number of results to be returned in a query. This should be a small value for quicker load times.",
                example: 10,
              },
            },
            $$ref:
              "http://localhost:5173/api-mapping/mef.sonata.api.serviceability.address#/components/schemas/LoopBackFilter",
          },
          examples: {
            searchBySpeeds: {
              summary: "Search DCFs by Speed.",
              value:
                '{\n   "where": {\n      "dataCenterFacility.speeds.value": 10000,\n      "dataCenterFacility.ready": true\n   },\n   "skip": 0,\n   "limit": 10\n}',
            },
            searchByLocation: {
              summary: "Search DCFs by location.",
              value:
                '{\n   "where": {\n      "company.addresses.country": "AU",\n      "company.addresses.city": "Sydney"\n      "dataCenterFacility.speeds.value": 10000,\n      "dataCenterFacility.ready": true\n   },\n   "skip": 0,\n   "limit": 10\n}',
            },
            searchByName: {
              summary: "Search DCFs by location.",
              value:
                '{\n   "where": {\n      "name": { "like": "DCF Name", options: "i" },\n   },\n   "skip": 0,\n   "limit": 10\n}',
            },
            searchByCrossConnectAddOn: {
              summary: "Search DCFs by CrossConnect Add On.",
              value:
                '{\n   "where": {\n      "dataCenterFacility.canBundleCrossConnect": true,\n   },\n   "skip": 0,\n   "limit": 10\n}',
            },
            searchByFreeCrossConnect: {
              summary: "Search DCFs by Free CrossConnect.",
              value:
                '{\n   "where": {\n      "tags": {"in": "free-cross-connect"}\n   },\n   "skip": 0,\n   "limit": 10\n}',
            },
          },
        },
      ],
      responses: {
        "200": {
          description: "returns a list of DCFs or an empty array",
          content: {
            "application/json": {
              examples: {
                results: {
                  value: [
                    {
                      id: "507f1f77bcf86cd799439011",
                      name: "PCCW Global - Hermes House",
                      username: "pccwg-hermes-house",
                      company: {
                        addresses: [
                          {
                            street:
                              "5/F, Hermes House,10 Middle Road,Tsim Sha Tsui",
                            city: "Hong Kong",
                            country: "CN",
                          },
                        ],
                      },
                      dataCenterFacility: {
                        metroId: "507f1f77bcf86cd799439011",
                        ready: true,
                        nniReady: true,
                        speeds: [
                          {
                            name: "10000 Mbps",
                            value: 10000,
                          },
                        ],
                        canBundleCrossConnect: false,
                      },
                    },
                  ],
                },
                empty: {
                  value: [],
                },
              },
            },
          },
        },
      },
      security: [
        {
          "api-key": [],
        },
      ],
    },
  });
});

test("NewAPIMapping page", () => {
  const { container, getAllByText, getByTestId, getByText } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <RequestItem
          item={{
            id: "id",
            name: "address.validation.country",
            title: "The country that the address is in",
            source: "@{{submittedGeographicAddress.country}}",
            target:
              'filter={"where":{"company.addresses.country":"@{{submittedGeographicAddress.country}}","company.addresses.city":"@{{submittedGeographicAddress.city}}"},"skip":0,"limit":10}',
            description: "",
            sourceLocation: "BODY",
            targetLocation: "",
            requiredMapping: true,
          }}
          index={0}
        />
        <RightAddSellerProp />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
  const examples = getAllByText("Add value with variable");
  expect(examples?.length).greaterThanOrEqual(1);
  fireEvent.click(examples[0]);
  waitFor(() => {
    const item = getByTestId("example-text-area");
    expect(item).toBeInTheDocument();
    fireEvent.change(item, { target: { value: "example-hybrid" } });
    const ok = getByTestId("example-btn");
    fireEvent.click(ok);
    const okSeller = getByTestId("seller-prop-ok");
    fireEvent.click(okSeller);
    const editExample = getByText("Edit value with variable");
    expect(editExample).toBeInTheDocument();
    fireEvent.click(editExample);
    fireEvent.change(item, { target: { value: "" } });
    fireEvent.click(ok);
    fireEvent.click(okSeller);
  });
});
