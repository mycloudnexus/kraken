import { buildInitListMapping } from "@/pages/NewAPIMapping";
import ResponseMapping from "@/pages/NewAPIMapping/components/ResponseMapping";
import RightAddSellerProp from "@/pages/NewAPIMapping/components/RightAddSellerProp";
import RightAddSonataProp, {
  getCorrectSpec,
} from "@/pages/NewAPIMapping/components/RightAddSonataProp";
import { APIItem } from "@/pages/NewAPIMapping/components/SelectAPI";
import SelectResponseProperty from "@/pages/NewAPIMapping/components/SelectResponseProperty";
import SonataPropMapping from "@/pages/NewAPIMapping/components/SonataPropMapping";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, renderHook } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

beforeAll(() => {
  const { result } = renderHook(() => useNewApiMappingStore());
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

test("parse fnc", () => {
  const result = buildInitListMapping([
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
        a: "inProgress.draft",
        b: "inProgress.draft",
        dd: "accepted",
        ee: "inProgress",
        fff: "inProgress",
      },
      sourceLocation: "BODY",
      targetLocation: "BODY",
      requiredMapping: true,
    },
  ]);
  expect(result).toEqual([
    {
      from: "accepted",
      key: 1,
      name: "mapper.quote.uni.add.state",
      to: ["1", "2", "dd"],
    },
    {
      from: "inProgress.draft",
      key: 2,
      name: "mapper.quote.uni.add.state",
      to: ["a", "b"],
    },
    {
      from: "inProgress",
      key: 3,
      name: "mapper.quote.uni.add.state",
      to: ["ee", "fff"],
    },
  ]);
});

test("component new api map page", async () => {
  const { container, getByTestId, getAllByTestId, getAllByPlaceholderText } =
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <ResponseMapping />
          <SelectResponseProperty />
        </BrowserRouter>
      </QueryClientProvider>
    );
  expect(container).toBeInTheDocument();
  const element = getByTestId("btn-add-state");
  fireEvent.click(element);
  const select = getAllByTestId("select-sonata-state");
  expect(select.length).toBeGreaterThanOrEqual(1);
  const input = getAllByPlaceholderText("Select response property");
  fireEvent.change(input[0], { target: { value: "a" } });
  fireEvent.keyDown(input[0], { key: "Enter", code: "Enter" });
});

test("component RightAddSellerProp", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <RightAddSellerProp />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("component RightAddSonataProp", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <RightAddSonataProp spec={undefined} method="GET" />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("function getCorrectSpec", () => {
  const spec = {
    paths: {
      "/a/b/c/d": {
        get: {
          a: 1,
        },
        post: {
          a: 2,
        },
      },
    },
  };
  expect(getCorrectSpec(spec, "get")).toEqual({ a: 1 });
  expect(getCorrectSpec(spec, "post")).toEqual({ a: 2 });
});

test("APIItem", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <APIItem
          isOneItem={false}
          item={
            {
              kind: "kraken.component.api-target-spec",
              metadata: {
                id: "b7d7b3ee-f336-4066-97f9-23bd39ec1a82",
                name: "console connect 12",
                version: 0,
                key: "mef.sonata.api-target-spec.con1718940696857",
                description: "ABC",
              },
              facets: {
                environments: {},
                selectedAPIs: ["/productOrder/{id} patch"],
                baseSpec: {
                  path: "http://localhost:5173/component/mef.sonata.api.poq/new",
                },
              },
            } as any
          }
          setSellerApi={vi.fn()}
          selectedAPI={"ABC"}
          setSelectedServer={vi.fn()}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("SonataPropMapping", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <SonataPropMapping
          list={
            [
              {
                title: "Property mapping",
                source: "@{{query.buyerId}}",
                target: "@{{path.companyName}}",
                sourceLocation: "QUERY",
                targetLocation: "PATH",
              },
            ] as any
          }
          title={"Property mapping"}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
