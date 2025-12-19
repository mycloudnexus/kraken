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
    // Mock recentXDays to return distinct strings for verification
    recentXDays: vi.fn((days) => ({
      requestStartTime: `2025-01-01-${days}`, 
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

  test("handleFormValues: clearing RequestTime defaults to recent 7 days (covers '|| 7' default)", async () => {
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

  test("Coverage: Changing Environment while in 'Recent 90 days' mode (covers Lines 56, 64, 65)", async () => {
    const { getActivitySpy } = setupSpies();
    const { getByTestId, getAllByTestId } = render(<ActivityDiagrams envs={baseEnvs.data} />);

    // 1. Switch to 90 days
    fireEvent.click(getByTestId("recent-90-days"));
    await waitFor(() => expect(recentXDaysMock).toHaveBeenLastCalledWith(90));

    // 2. Change Environment
    const changeEnvBtn = getAllByTestId(/mock-select-change/)[0]; 
    fireEvent.click(changeEnvBtn);

    // 3. Verify Logic
    // We expect the productId to be "mef.sonata" (fallback behavior) or simply ignore it as it's not the SUT here.
    // We strictly check the params that handleFormValues modifies.
    await waitFor(() => {
      expect(getActivitySpy).toHaveBeenLastCalledWith(
        expect.anything(), // ProductId (received "mef.sonata")
        "NEW_VALUE_ID",    // envId updated
        "2025-01-01-90",   // Dates maintained for 90 days
        "2025-01-07",
        "ALL_BUYERS"       // Buyer maintained
      );
    });
  });

  test("Coverage: Changing Buyer while in 'Specific Date Range' mode (covers Lines 74, 75, 77, 79)", async () => {
    const { getActivitySpy } = setupSpies();
    const { getByTestId, getAllByTestId } = render(<ActivityDiagrams envs={baseEnvs.data} />);

    // 1. Select Date Range
    fireEvent.click(getByTestId("mock-picker-select-btn"));

    // 2. Change Buyer
    const changeBuyerBtn = getAllByTestId(/mock-select-change/)[1]; 
    fireEvent.click(changeBuyerBtn);

    // 3. Verify Logic
    await waitFor(() => {
      expect(getActivitySpy).toHaveBeenLastCalledWith(
        productId,
        prodEnvId,
        expect.stringContaining("2025-10-01"),
        expect.stringContaining("2025-10-05"),
        "NEW_VALUE_ID" // buyer updated
      );
    });
  });
});