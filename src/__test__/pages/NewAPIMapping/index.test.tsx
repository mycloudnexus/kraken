import { render, renderHook } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import NewAPIMapping from "@/pages/NewAPIMapping";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";

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

test("NewAPIMapping page", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <NewAPIMapping />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
