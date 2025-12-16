import { render, waitFor } from "@/__test__/utils";
import * as homepageHooks from "@/hooks/homepage";
import ActivityDiagrams from "@/pages/HomePage/components/ActivityDiagrams";
import { fireEvent } from "@testing-library/react";
import { recentXDays } from "@/utils/constants/format"; // Import directly

beforeEach(() => {
  vi.clearAllMocks();
});

// 1. Update Mock 'antd' to include a "Select Range" button
vi.mock("antd", async () => {
  const actual = await vi.importActual<any>("antd");

  const MockRangePicker = (props: any) => (
    <div data-testid="mock-range-picker">
      {/* Button to simulate CLEAR (covers the if block) */}
      <button
        data-testid="mock-picker-clear-btn"
        onClick={() => props.onChange && props.onChange(null)}
      >
        Simulate Clear
      </button>

      {/* NEW: Button to simulate SELECTING values (covers the else block) */}
      <button
        data-testid="mock-picker-select-btn"
        onClick={() =>
          props.onChange &&
          props.onChange(["2025-10-01", "2025-10-05"])
        }
      >
        Simulate Select Range
      </button>
    </div>
  );

  return {
    ...actual,
    DatePicker: {
      ...actual.DatePicker,
      RangePicker: MockRangePicker,
    },
  };
});

// 2. Mock utils
vi.mock("@/utils/constants/format", async () => {
  const actual = await vi.importActual<any>("@/utils/constants/format");
  return {
    ...actual,
    recentXDays: vi.fn(() => ({
      requestStartTime: "2025-01-01",
      requestEndTime: "2025-01-07",
    })),
  };
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

const recentXDaysMock = recentXDays as unknown as ReturnType<typeof vi.fn>;

describe("ActivityDiagrams Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const baseEnvs = {
    data: [
      {
        id: "32b4832f-fb2f-4c99-b89a-c5c995b18dfc",
        productId: "mef.sonata",
        createdAt: "2024-05-30T13:02:03.224486Z",
        name: "production",
      },
    ],
  };

  test("handleFormValues: clearing RequestTime resets dates to recent 7 days", async () => {
    const refetchActivity = vi.fn();

    // Mock all hooks with necessary properties (including refetch)
    vi.spyOn(homepageHooks, "useGetActivityRequests").mockReturnValue({
      data: { requestStatistics: [] },
      isLoading: false,
      refetch: refetchActivity,
      isRefetching: false,
    } as any);

    vi.spyOn(homepageHooks, "useGetErrorBrakedown").mockReturnValue({
      data: { errorBreakdowns: [] },
      isLoading: false,
      refetch: vi.fn(),
    } as any);

    vi.spyOn(homepageHooks, "useGetMostPopularEndpoints").mockReturnValue({
      data: { endpointUsages: [] },
      refetch: vi.fn(),
      isLoading: false,
      isFetching: false,
      isFetched: true,
    } as any);

    const { getByTestId } = render(<ActivityDiagrams envs={baseEnvs.data} />);

    // 1. Verify the Mock Picker rendered
    const clearBtn = getByTestId("mock-picker-clear-btn");
    expect(clearBtn).toBeInTheDocument();

    // Clear calls from render
    recentXDaysMock.mockClear();

    // 2. Trigger the Clear Action
    fireEvent.click(clearBtn);

    // 3. Verify handleFormValues logic
    await waitFor(() => {
      expect(recentXDaysMock).toHaveBeenCalledWith(7);
    });

    expect(refetchActivity).toHaveBeenCalled();
  });

  test("handleFormValues: selecting a specific date range updates params (covers else block)", async () => {
    const refetchActivity = vi.fn();

    // Setup Hooks
    vi.spyOn(homepageHooks, "useGetActivityRequests").mockReturnValue({
      data: { requestStatistics: [] },
      isLoading: false,
      refetch: refetchActivity,
      isRefetching: false,
    } as any);

    vi.spyOn(homepageHooks, "useGetErrorBrakedown").mockReturnValue({
      data: { errorBreakdowns: [] },
      isLoading: false,
      refetch: vi.fn(),
    } as any);

    vi.spyOn(homepageHooks, "useGetMostPopularEndpoints").mockReturnValue({
      data: { endpointUsages: [] },
      refetch: vi.fn(),
      isLoading: false,
      isFetching: false,
      isFetched: true,
    } as any);

    const { getByTestId } = render(<ActivityDiagrams envs={baseEnvs.data} />);

    // 1. Verify the Select button is present
    const selectBtn = getByTestId("mock-picker-select-btn");
    
    // 2. Click "Simulate Select Range"
    // This sends ["2025-10-01", "2025-10-05"] to the Form
    // It causes `!requestTime` to be false, entering the ELSE block
    fireEvent.click(selectBtn);

    // 3. Verify side effects
    await waitFor(() => {
      // The code should set `setSelectedRecentDate(undefined)`
      // We can't check internal state directly, but we can check the side effect:
      // A refetch should occur with the NEW parameters.
      expect(refetchActivity).toHaveBeenCalled();
    });

    // NOTE: This test covers the following lines:
    // - setSelectedRecentDate(undefined);
    // - setParams((prev) => ({ ... }));
    // - parseDateStartOrEnd(...)
  });

  test("renders correctly with data (Integration Test)", () => {
    // 1. Mock Activity Requests
    vi.spyOn(homepageHooks, "useGetActivityRequests").mockReturnValue({
      data: {
        requestStatistics: [{ date: "2024-05-30", success: 1, error: 2 }],
      },
      isLoading: false,
      refetch: vi.fn(),
      isRefetching: false,
    } as any);

    // 2. Mock Error Breakdown (MUST include refetch)
    vi.spyOn(homepageHooks, "useGetErrorBrakedown").mockReturnValue({
      data: { errorBreakdowns: [] },
      isLoading: false,
      refetch: vi.fn(), // <--- This was missing
    } as any);

    // 3. Mock Popular Endpoints (MUST include refetch)
    vi.spyOn(homepageHooks, "useGetMostPopularEndpoints").mockReturnValue({
      data: { endpointUsages: [] },
      isLoading: false,
      isFetching: false,
      isFetched: true,
      refetch: vi.fn(), // <--- This was missing
    } as any);

    const { getByText, getByTestId } = render(
      <ActivityDiagrams envs={baseEnvs.data} />
    );

    // Verify UI
    expect(getByText("API activity dashboard")).toBeInTheDocument();

    // Verify interaction triggers hook update
    const recentButton = getByTestId("recent-90-days");
    fireEvent.click(recentButton);
    expect(recentXDaysMock).toHaveBeenCalledWith(90);
  });



  test("ActivityDiagrams test with data (Original Test)", () => {
    vi.spyOn(homepageHooks, "useGetErrorBrakedown").mockReturnValue({
      data: {
        errorBreakdowns: [
          {
            date: "2024-05-30T13:02:03.224486Z",
            errors: { 400: 1, 401: 1, 404: 1, 500: 1 },
          },
        ],
      },
      isLoading: false,
      refetch: vi.fn(),
    } as any);

    vi.spyOn(homepageHooks, "useGetActivityRequests").mockReturnValue({
      data: {
        requestStatistics: [
          { date: "2024-05-30T13:02:03.224486Z", success: 1, error: 2 },
        ],
      },
      isLoading: false,
      refetch: vi.fn(),
      isRefetching: false,
    } as any);

    const popularEndpoints = [
      {
        method: "GET",
        endpoint: "/test-endpoint",
        usage: 11,
        popularity: 33.33,
      },
    ];
    vi.spyOn(homepageHooks, "useGetMostPopularEndpoints").mockReturnValue({
      data: { endpointUsages: popularEndpoints },
      refetch: () => popularEndpoints,
      isLoading: false,
      isFetching: false,
      isFetched: true,
    } as any);

    const { container, getByText, getAllByTestId } = render(
      <ActivityDiagrams envs={baseEnvs.data} />
    );
    expect(container).toBeInTheDocument();

    expect(getByText("Most popular endpoints")).toBeInTheDocument();
    expect(getAllByTestId("method")[0]).toHaveTextContent("GET");
    expect(getAllByTestId("usage")[0]).toHaveTextContent("11");
  });

  test("ActivityDiagrams test with no data (Original Test)", async () => {
    const stageEnvs = {
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
      data: { errorBreakdowns: [] },
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
      data: { endpointUsages: [] },
      refetch: () => [],
      isLoading: false,
      isFetching: false,
      isFetched: true,
    } as any);

    const { container, getByText } = render(
      <ActivityDiagrams envs={stageEnvs.data} />
    );
    expect(container).toBeInTheDocument();

    expect(
      getByText("When errors occur, they will be displayed here.")
    ).toBeInTheDocument();
  });
});