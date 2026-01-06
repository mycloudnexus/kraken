import NewAPIMapping from "@/pages/NewAPIMapping";
import * as newApiMappingHooks from "@/stores/newApiMapping.store";
import { render, renderHook } from "@/__test__/utils";
import HeaderMapping from "@/pages/NewAPIMapping/components/HeaderMapping";
import * as mappingUiStore from "@/stores/mappingUi.store";


beforeEach(() => {
  vi.clearAllMocks()
})

beforeAll(() => {
  const { result } = renderHook(() => newApiMappingHooks.useNewApiMappingStore());
  result.current.setResponseMapping([
    {
      name: "mapper.quote.uni.add.state",
      title: "Quote State Mapping",
      source: "@{{responseBody.id}}",
      target: "@{{quoteItem[*].state}}",
      targetType: "enum",
      description: "quote state mapping",
      targetValues: [
        "accepted",
        "acknowledged",
        "answered",
        "approved.orderable",
        "approved.orderableAlternate",
        "inProgress",
        "inProgress.draft",
        "abandoned",
        "rejected",
        "unableToProvide",
      ],
      valueMapping: {
        "1": "accepted",
        "2": "accepted",
        "3": "accepted",
        "4": "accepted",
        a: "inProgress.draft",
        b: "inProgress.draft",
        c: "inProgress.draft",
        d: "inProgress.draft",
        dd: "accepted",
        ee: "inProgress",
        fff: "inProgress",
      },
      sourceLocation: "BODY",
      targetLocation: "BODY",
      requiredMapping: true,
    },
    {
      name: "mapper.quote.uni.add.price.value",
      title: "Quote Price Value Mapping",
      source: "@{{responseBody.externalId}}",
      target: "@{{quoteItem.quoteItemPrice.price.dutyFreeAmount.value}}",
      description: "quote price mapping",
      sourceLocation: "BODY",
      targetLocation: "BODY",
      requiredMapping: true,
    },
    {
      name: "mapper.quote.uni.add.price.unit",
      title: "Quote Price Unit Mapping",
      source: "@{{responseBody.orderDate}}",
      target: "@{{quoteItem.quoteItemPrice.price.dutyFreeAmount.unit}}",
      description: "quote price mapping",
      sourceLocation: "BODY",
      targetLocation: "BODY",
      requiredMapping: true,
    },
    {
      name: "mapper.quote.uni.add.duration.amount",
      title: "Quote Duration Amount Mapping",
      source: "@{{responseBody.cancellationDate}}",
      target: "@{{quoteItem.requestedQuoteItemTerm.duration.amount}}",
      description: "quote duration amount mapping",
      sourceLocation: "BODY",
      targetLocation: "BODY",
      requiredMapping: true,
    },
    {
      name: "mapper.quote.uni.add.duration.units",
      title: "Quote Duration Units Mapping",
      source: "@{{responseBody.state}}",
      target: "@{{quoteItem[*].requestedQuoteItemTerm.duration.units}}",
      targetType: "enum",
      description: "quote duration units mapping",
      sourceLocation: "BODY",
      targetLocation: "BODY",
      requiredMapping: true,
    },
  ]);
  result.current.setListMappingStateResponse([
    {
      from: "accepted",
      to: ["1", "2", "3", "4", "dd"],
      key: 1,
      name: "mapper.quote.uni.add.state",
    },
    {
      from: "inProgress.draft",
      to: ["a", "b", "c", "d"],
      key: 2,
      name: "mapper.quote.uni.add.state",
    },
    {
      from: "inProgress",
      to: ["ee", "fff"],
      key: 3,
      name: "mapper.quote.uni.add.state",
    },
  ]);
});

test("NewAPIMapping page", () => {
  const { container } = render(
    <>
      <NewAPIMapping isRequiredMapping={true} />
      <NewAPIMapping isRequiredMapping={false} />
    </>
  );
  expect(container).toBeInTheDocument();
});

describe('api mapping components tests', () => {
  it('should render mapping header component', () => {
    const setRightSide = vi.fn()
    const setRequestMapping = vi.fn()
    const setSellerApi = vi.fn()
    const setServerKey = vi.fn()
    const setListMappingStateResponse = vi.fn()

    vi.spyOn(newApiMappingHooks, 'useNewApiMappingStore').mockReturnValue({
      sellerApi: {
        method: 'GET',
        url: 'seller/a/b/c/d',

      },
      query: JSON.stringify({
        method: 'POST',
        path: 'query/path',
      }),
      rightSide: 0,
      setRightSide,
      setRequestMapping,
      setSellerApi,
      setServerKey,
      setListMappingStateResponse
    })

    const { getByTestId } = render(<HeaderMapping mappers={{
      request: [],
      response: []
    }} />)

    expect(getByTestId('sonataApi')).toHaveTextContent('Sonata API')
    expect(getByTestId('sellerApi')).toHaveTextContent('Seller API')
  })
})

describe("NewAPIMapping - activeTab reset behavior", () => {
  it("should set activeTab to request on mount and unmount", () => {
    const setActiveTab = vi.fn();
    vi.spyOn(mappingUiStore, "useMappingUiStore").mockReturnValue({
      activeTab: "response",
      setActiveTab,
    } as any);
    const { unmount } = render(
      <NewAPIMapping isRequiredMapping={true} />
    );

    expect(setActiveTab).toHaveBeenCalledWith("request");
    setActiveTab.mockClear();
    unmount();
    expect(setActiveTab).toHaveBeenCalledWith("request");
  });
});
