import { fireEvent, render, renderHook, waitFor } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import SelectResponseProperty from "@/pages/NewAPIMapping/components/SelectResponseProperty";
import ResponseMapping from "@/pages/NewAPIMapping/components/ResponseMapping";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";

beforeAll(() => {
  const { result } = renderHook(() => useNewApiMappingStore());
  result.current.setResponseMapping([
    {
      name: "mapper.order.uni.add.state",
      title: "Order State Mapping",
      source: "",
      target: "@{{status}}",
      targetType: "enum",
      description: "Please map order status between Sonata API and Seller API",
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
      valueMapping: "",
      sourceLocation: "BODY",
      targetLocation: "BODY",
    },
    {
      id: "orderId",
      name: "mapper.order.uni.add.orderId",
      title: "Order Id location",
      source: "",
      description:
        "Please specify the field that represent the order Id from Seller API response",
      sourceLocation: "",
    },
    {
      id: "instanceId",
      name: "mapper.order.uni.add.instanceId",
      title: "Instance Id location",
      source: "",
      description:
        "Please specify the field that represent the instance Id from Seller API response",
      sourceLocation: "",
    },
  ]);
});

test("component new api map page", () => {
  const { container, getByTestId } = render(
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
  waitFor(() => {
    const select = getByTestId("select-sonata-state");
    expect(select).toBeInTheDocument();
    fireEvent.select(select, { target: { value: "acknowledged" } });
    fireEvent.click(element);
  });
});
