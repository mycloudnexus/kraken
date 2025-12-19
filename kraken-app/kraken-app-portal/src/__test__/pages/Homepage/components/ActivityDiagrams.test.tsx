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
      <button
        data-testid="mock-picker-invalid-btn"
        onClick={() =>
          props.onChange && props.onChange(["INVALID", "INVALID"])
        }
      >
        Simulate Invalid Date
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
      <button
        data-testid={`mock-select-clear-${props.className}`} 
        onClick={() => props.onChange && props.onChange(null)}
      >
        Clear Value
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
      requestStartTime: `2025-01-01-${days}`, 
      requestEndTime: "2025-01-07",
    })),
    parseDateStartOrEnd: vi.fn((date, type) => {
      // Return undefined for specific "INVALID" string to test '?? undefined' fallback
      if (date === "INVALID") return undefined;
      return actual.parseDateStartOrEnd(date, type);
    }),
  };
});

const recentXDaysMock = recentXDays as unknown as ReturnType<typeof vi.fn>;

describe("ActivityDiagrams Component - Code Coverage", () => {
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

  test("renders correctly when envs is undefined (covers Line 88: '?? []')", () => {
    const { getActivitySpy } = setupSpies();
    
    // FIX: Cast undefined to any to bypass strict prop type checking for this defensive test
    render(<ActivityDiagrams envs={undefined as any} />);
    
    // Verify it doesn't crash and default logic runs
    expect(getActivitySpy).toHaveBeenCalled();
  });

  test("handleFormValues: clearing RequestTime defaults to recent 7 days (covers '|| 7' and Line 53 default)", async () => {
    const { getActivitySpy } = setupSpies();
    const { getByTestId } = render(<ActivityDiagrams envs={baseEnvs.data} />);

    await waitFor(() => {
      expect(recentXDaysMock).toHaveBeenLastCalledWith(7);
    });

    const clearBtn = getByTestId("mock-picker-clear-btn");
    fireEvent.click(clearBtn);

    await waitFor(() => {
      expect(recentXDaysMock).toHaveBeenLastCalledWith(7);
      
      expect(getActivitySpy).toHaveBeenLastCalledWith(
        productId,
        prodEnvId,
        "2025-01-01-7", 
        "2025-01-07",
        "ALL_BUYERS"
      );
    });
  });

  test("handleFormValues: Fallback logic when form values missing (covers Lines 64, 65, 74, 75)", async () => {
    const { getActivitySpy } = setupSpies();
    const { getByTestId, getAllByTestId } = render(<ActivityDiagrams envs={baseEnvs.data} />);

    // 1. Clear "envId" (Trigger Else Block fallback)
    const clearEnvBtn = getAllByTestId(/mock-select-clear/)[0];
    fireEvent.click(clearEnvBtn); 

    const selectDateBtn = getByTestId("mock-picker-select-btn");
    fireEvent.click(selectDateBtn);

    await waitFor(() => {
      expect(getActivitySpy).toHaveBeenLastCalledWith(
        productId,
        prodEnvId, // Falls back to prev.envId
        expect.stringContaining("2025-10-01"),
        expect.stringContaining("2025-10-05"),
        "ALL_BUYERS"
      );
    });

    // 2. Clear "buyer" (Trigger If Block fallback)
    const clearBuyerBtn = getAllByTestId(/mock-select-clear/)[1];
    fireEvent.click(clearBuyerBtn);

    const clearDateBtn = getByTestId("mock-picker-clear-btn");
    fireEvent.click(clearDateBtn);

    await waitFor(() => {
       expect(getActivitySpy).toHaveBeenLastCalledWith(
        productId,
        prodEnvId,
        expect.any(String),
        expect.any(String),
        "ALL_BUYERS" // Falls back to prev.buyer
      );
    });
  });

  test("handleFormValues: handling invalid dates (covers Lines 77, 79 '?? undefined')", async () => {
    const { getActivitySpy } = setupSpies();
    const { getByTestId } = render(<ActivityDiagrams envs={baseEnvs.data} />);

    // This sends "INVALID" which our mock converts to undefined
    const invalidDateBtn = getByTestId("mock-picker-invalid-btn");
    fireEvent.click(invalidDateBtn);

    await waitFor(() => {
      expect(getActivitySpy).toHaveBeenLastCalledWith(
        productId,
        prodEnvId,
        undefined, // Fallback triggered
        undefined, // Fallback triggered
        "ALL_BUYERS"
      );
    });
  });

  test("Coverage: Changing Environment while in 'Recent 90 days' mode (covers Lines 56, 64, 65 integration)", async () => {
    const { getActivitySpy } = setupSpies();
    const { getByTestId, getAllByTestId } = render(<ActivityDiagrams envs={baseEnvs.data} />);

    // 1. Switch to 90 days
    fireEvent.click(getByTestId("recent-90-days"));
    await waitFor(() => expect(recentXDaysMock).toHaveBeenLastCalledWith(90));

    // 2. Change Environment
    const changeEnvBtn = getAllByTestId(/mock-select-change/)[0]; 
    fireEvent.click(changeEnvBtn);

    await waitFor(() => {
      expect(getActivitySpy).toHaveBeenLastCalledWith(
        expect.anything(), 
        "NEW_VALUE_ID", 
        "2025-01-01-90", 
        "2025-01-07",
        "ALL_BUYERS" 
      );
    });
  });
});