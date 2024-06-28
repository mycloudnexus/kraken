import ResponseMapping from "@/pages/NewAPIMapping/components/ResponseMapping";
import RightAddSellerProp from "@/pages/NewAPIMapping/components/RightAddSellerProp";
import RightAddSonataProp, {
  getCorrectSpec,
} from "@/pages/NewAPIMapping/components/RightAddSonataProp";
import { APIItem } from "@/pages/NewAPIMapping/components/SelectAPI";
import SelectResponseProperty from "@/pages/NewAPIMapping/components/SelectResponseProperty";
import { useNewApiMappingStore } from "@/stores/newApiMapping.store";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, renderHook } from "@testing-library/react";
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

test("component new api map page", async () => {
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
  const select = getByTestId("select-sonata-state");
  expect(select).toBeInTheDocument();
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
