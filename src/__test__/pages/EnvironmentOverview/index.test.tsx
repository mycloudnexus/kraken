import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import EnvironmentOverview from "@/pages/EnvironmentOverview";
import * as productHooks from "@/hooks/product";

test("EnvironmentOverview page", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <EnvironmentOverview />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

describe(" EnvironmentOverview   component list", () => {
  beforeAll(() => {
    vi.spyOn(productHooks, "useGetProductEnvs").mockReturnValue({
      data: {
        data: [
          {
            id: "32b4832f-fb2f-4c99-b89a-c5c995b18dfc",
            productId: "mef.sonata",
            createdAt: "2024-05-30T13:02:03.224486Z",
            name: "production",
          },
        ],
      },
      isLoading: false,
    } as any);
    vi.spyOn(productHooks, "useGetRunningAPIList").mockReturnValue({
      data: [
        {
          id: "b2d775e5-44ad-43cb-8dd4-6fbe52585ec9",
          createdAt: "2024-08-01 01:55:27",
          updatedAt: "2024-08-01 01:55:27",
          name: "production",
          components: [
            {
              version: "1.0",
              key: "mef.sonata.api.order",
              componentName: "Product Ordering Management"
            },
            {
              version: "1.1",
              key: "mef.sonata.api.inventory",
              componentName: "Product Inventory Management"
            },
            {
              version: "1.0",
              key: "mef.sonata.api.serviceability.address",
              componentName: "Geographic Address Management"
            },
            {
              version: "1.0",
              key: "mef.sonata.api.quote",
              componentName: "Quote Management"
            }
          ]
        }
      ],
      isLoading: false,
    } as any);
  })

  it("running components list", () => {
    const { getByText } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <EnvironmentOverview />
        </BrowserRouter>
      </QueryClientProvider>
    );
    const ele = getByText("production");
    expect(ele).toBeInTheDocument();
  });
});

describe(" EnvironmentOverview   component list stage", () => {
  beforeAll(() => {
    vi.spyOn(productHooks, "useGetProductEnvs").mockReturnValue({
      data: {
        data: [
          {
            id: "32b4832f-fb2f-4c99-b89a-c5c995b18dfc",
            productId: "mef.sonata",
            createdAt: "2024-05-30T13:02:03.224486Z",
            name: "stage",
          },
        ],
      },
      isLoading: false,
    } as any);
    vi.spyOn(productHooks, "useGetRunningAPIList").mockReturnValue({
      data: [
        {
          id: "32b4832f-fb2f-4c99-b89a-c5c995b18dfc",
          createdAt: "2024-06-18T02:00:06.730627Z",
          name: "stage",
          status: "IN_PROCESS",
          version: "1.0",
          key: "mef.sonata.api.quote",
          componentName: "testname",
        },
      ],
      isLoading: false,
    } as any);
  })

  afterAll(() => {
    vi.clearAllMocks();
  })

  it("running components list", () => {
    const { getByText } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <EnvironmentOverview />
        </BrowserRouter>
      </QueryClientProvider>
    );
    const ele = getByText("testname");
    expect(ele).toBeInTheDocument();
  });
});
