import { render, waitFor, fireEvent } from "@/__test__/utils";
import * as homepageHooks from "@/hooks/homepage";
import ActivityDiagrams from "@/pages/HomePage/components/ActivityDiagrams";
import { recentXDays } from "@/utils/constants/format";

beforeEach(() => {
  vi.clearAllMocks();
});

// 1. Mock 'antd' components
vi.mock("antd", async () => {
  const actual = await vi.importActual<any>("antd");

  const MockRangePicker = (props: any) => (
    <div data-testid="mock-range-picker">
      <button
        data-testid="mock-picker-clear-btn"
        onClick={() => props.onChange && props.onChange(null)}
      >
        Simulate Clear
      </button>
      <button
        data-testid="mock-picker-select-btn"
        onClick={() =>
          props.onChange && props.onChange(["2025-10-01", "2025-10-05"])
        }
      >
        Simulate Select Range
      </button>
    </div>
  );

  const MockSelect = (props: any) => (
    <div data-testid="mock-select">
      <div data-testid={`select-value-${props.value}`}>{props.value}</div>
      <button
        data-testid={`mock-select-change-${props.className}`} 
        onClick={() => props.onChange && props.onChange("NEW_VALUE_ID")}
      >
        Change Value
      </button>
    </div>
  );

  return {
    ...actual,
    DatePicker: {
      ...actual.DatePicker,
      RangePicker: MockRangePicker,
    },
    Select: MockSelect,
  };
});

// 2. Mock utils
vi.mock("@/utils/constants/format", async () => {
  const actual = await vi.importActual<any>("@/utils/constants/format");
  return {
    ...actual,
    recentXDays: vi.fn((days) => ({
      requestStartTime: `2025-01-01-${days}`, // Dynamic return based on input
      requestEndTime: "2025-01-07",
    })),
  };
});

const recentXDaysMock = recentXDays as unknown as ReturnType<typeof vi.fn>;

describe("ActivityDiagrams Component - handleFormValues Logic", () => {
  const prodEnvId = "32b4832f-fb2f-4c99-b89a-c5c995b18dfc";
  const productId = "mef.sonata";
  
  const baseEnvs = {
    data: [
      {
        id: prodEnvId,
        productId: productId,
        createdAt: "2024-05-30T13:02:03.224486Z",
        name: "production",
      },
      {
        id: "stage-id",
        productId: productId,
        createdAt: "2024-05-30T13:02:03.224486Z",
        name: "stage",
      }
    ],
  };

  const setupSpies = () => {
    const getActivitySpy = vi.spyOn(homepageHooks, "useGetActivityRequests").mockReturnValue({
      data: { requestStatistics: [] },
      isLoading: false,
      refetch: vi.fn(),
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

    return { getActivitySpy };
  };

  test("handleFormValues: clearing RequestTime defaults to recent 7 days (covering '|| 7')", async () => {
    const { getActivitySpy } = setupSpies();
    const { getByTestId } = render(<ActivityDiagrams envs={baseEnvs.data} />);

    // 1. Initial State should be 7 days
    await waitFor(() => {
      expect(recentXDaysMock).toHaveBeenLastCalledWith(7);
    });

    // 2. Simulate User Clicking "Clear" on DatePicker
    // This triggers handleFormValues with requestTime: null
    // selectedRecentDate is undefined here (or default), so '|| 7' logic applies
    const clearBtn = getByTestId("mock-picker-clear-btn");
    fireEvent.click(clearBtn);

    // 3. Verify recentXDays called with 7
    await waitFor(() => {
      expect(recentXDaysMock).toHaveBeenLastCalledWith(7);
      
      // Verify API called with the dates generated for 7 days
      // NOTE: "buyer" is expected to be "ALL_BUYERS" because the useEffect sets the form value,
      // and handleFormValues reads it from the form values.
      expect(getActivitySpy).toHaveBeenLastCalledWith(
        productId,
        prodEnvId,
        "2025-01-01-7", // Matches our mock implementation
        "2025-01-07",
        "ALL_BUYERS"    // Updated expectation
      );
    });
  });

  test("handleFormValues: selecting 'Recent 90 days' updates params (covering selectedRecentDate)", async () => {
    const { getActivitySpy } = setupSpies();
    const { getByTestId } = render(<ActivityDiagrams envs={baseEnvs.data} />);

    // 1. Find and Click "Recent 90 days" Radio Button
    const recent90Btn = getByTestId("recent-90-days");
    fireEvent.click(recent90Btn);

    // 2. Verify recentXDays called with 90
    // This covers logic where selectedRecentDate state updates
    await waitFor(() => {
      expect(recentXDaysMock).toHaveBeenLastCalledWith(90);

      // Verify API called with dates for 90 days
      // NOTE: buyer is undefined here because setRecentDate uses existing params state (which initialized to undefined)
      // and does not read from form values.
      expect(getActivitySpy).toHaveBeenLastCalledWith(
        productId,
        prodEnvId,
        "2025-01-01-90", // Matches our mock implementation
        "2025-01-07",
        undefined
      );
    });
  });

  test("handleFormValues: selecting a specific date range updates params (covering else block)", async () => {
    const { getActivitySpy } = setupSpies();
    const { getByTestId } = render(<ActivityDiagrams envs={baseEnvs.data} />);

    // 1. Simulate Select Specific Date Range
    // This triggers handleFormValues with actual date array
    const selectBtn = getByTestId("mock-picker-select-btn");
    fireEvent.click(selectBtn);

    // 2. Verify API called with the specific dates from the mock button
    // It should NOT use recentXDays values here
    await waitFor(() => {
      expect(getActivitySpy).toHaveBeenLastCalledWith(
        productId,
        prodEnvId,
        expect.stringContaining("2025-10-01"), // From mock button
        expect.stringContaining("2025-10-05"), 
        expect.anything()
      );
    });
  });
});