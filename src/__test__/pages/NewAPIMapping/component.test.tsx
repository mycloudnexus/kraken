import ResponseMapping from "@/pages/NewAPIMapping/components/ResponseMapping";
import RightAddSellerProp from "@/pages/NewAPIMapping/components/RightAddSellerProp";
import RightAddSonataProp from "@/pages/NewAPIMapping/components/RightAddSonataProp";
import SelectResponseProperty from "@/pages/NewAPIMapping/components/SelectResponseProperty";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, renderHook, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

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