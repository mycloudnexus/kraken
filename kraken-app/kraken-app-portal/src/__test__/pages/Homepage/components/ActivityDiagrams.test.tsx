import { render } from "@/__test__/utils";
import * as homepageHooks from "@/hooks/homepage";
import ActivityDiagrams from "@/pages/HomePage/components/ActivityDiagrams";
import { fireEvent } from "@testing-library/react";

beforeEach(() => {
  vi.clearAllMocks();
});

test("ActivityDiagrams test with data", () => {
  const envs = {
    data: [
      {
        id: "32b4832f-fb2f-4c99-b89a-c5c995b18dfc",
        productId: "mef.sonata",
        createdAt: "2024-05-30T13:02:03.224486Z",
        name: "production",
      },
    ],
  };

  vi.spyOn(homepageHooks, "useGetErrorBrakedown").mockReturnValue({
    data: {
      errorBreakdowns: [
        {
          date: "2024-05-30T13:02:03.224486Z",
          errors: {
            400: 1,
            401: 1,
            404: 1,
            500: 1,
          },
        },
      ],
    },
    isLoading: false,
    refetch: vi.fn(),
  } as any);

  vi.spyOn(homepageHooks, "useGetActivityRequests").mockReturnValue({
    data: {
      requestStatistics: [
        {
          date: "2024-05-30T13:02:03.224486Z",
          success: 1,
          error: 2,
        },
      ],
    },
    isLoading: false,
    refetch: vi.fn(),
    isRefetching: false,
  } as any);

  const popularEndpoints = [
    {
      method: "GET",
      endpoint:
        "/mefApi/sonata/quoteManagement/v8/quote/43d1e7e9-a11b-4843-a6da-39086ec5ceb2",
      usage: 11,
      popularity: 33.33,
    },
  ];
  vi.spyOn(homepageHooks, "useGetMostPopularEndpoints").mockReturnValue({
    data: {
      endpointUsages: popularEndpoints,
    },
    refetch: () => popularEndpoints,
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any);

  const { container, getByTestId, getByText, getAllByTestId } = render(
    <ActivityDiagrams envs={envs.data} />
  );
  expect(container).toBeInTheDocument();
  const recentButton = getByTestId("recent-90-days");
  fireEvent.click(recentButton);

  // most popular endpoints
  expect(getByText("Endpoint name")).toBeInTheDocument();
  expect(getByText("Popularity")).toBeInTheDocument();
  expect(getByText("Usage")).toBeInTheDocument();
  expect(getByText("Most popular endpoints")).toBeInTheDocument();

  expect(getAllByTestId("method")[0]).toHaveTextContent("GET");
  expect(getAllByTestId("apiPath")[0]).toHaveTextContent(
    "v8/quote/43d1e7e9-a11b-4843-a6da-39086ec5ceb2"
  );
  expect(getAllByTestId("usage")[0]).toHaveTextContent("11");
});

test("ActivityDiagrams test with no data", async () => {
  const envs = {
    data: [
      {
        id: "32b4832f-fb2f-4c99-b89a-c5c995b18dfc",
        productId: "mef.sonata",
        createdAt: "2024-05-30T13:02:03.224486Z",
        name: "stage",
      },
    ],
  };

  vi.spyOn(homepageHooks, "useGetErrorBrakedown").mockReturnValue({
    data: {
      errorBreakdowns: [],
    },
    isLoading: false,
    refetch: vi.fn(),
  } as any);

  vi.spyOn(homepageHooks, "useGetActivityRequests").mockReturnValue({
    data: {},
    isLoading: false,
    isFetcing: false,
    refetch: vi.fn(),
    isRefetching: false,
  } as any);

  vi.spyOn(homepageHooks, "useGetMostPopularEndpoints").mockReturnValue({
    data: {
      endpointUsages: [],
    },
    refetch: () => [],
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any);

  const { container, getByText } = render(
    <ActivityDiagrams envs={envs.data} />
  );
  expect(container).toBeInTheDocument();

  expect(
    getByText(
      "When requests are made, request status data will be displayed here."
    )
  ).toBeInTheDocument();
  expect(
    getByText(
      "As endpoints are accessed, the most popular ones will be displayed here."
    )
  ).toBeInTheDocument();
  expect(
    getByText("When errors occur, they will be displayed here.")
  ).toBeInTheDocument();
});
