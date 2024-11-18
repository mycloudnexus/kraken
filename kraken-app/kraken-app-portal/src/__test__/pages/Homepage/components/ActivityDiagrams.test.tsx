import ActivityDiagrams from '@/pages/HomePage/components/ActivityDiagrams';
import { fireEvent } from "@testing-library/react";
import * as homepageHooks from '@/hooks/homepage'
import { render } from "@/__test__/utils";

test("ActivityDiagrams test with data", () => {
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
      errorBreakdowns: [
        {
          date: "2024-05-30T13:02:03.224486Z",
          errors: {
            400: 1,
            401: 1,
            404: 1,
            500: 1,
          }
        },
      ],
    },
    isLoading: false,
    refetch: vi.fn()
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
    isRefetching: false
  } as any);

  const { container, getByTestId } = render(
    <ActivityDiagrams envs={envs.data} />
  );
  expect(container).toBeInTheDocument();
  const recentButton = getByTestId('recent-90-days');
  fireEvent.click(recentButton);
});

test("ActivityDiagrams test with no data", () => {
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
    refetch: vi.fn()
  } as any);


  vi.spyOn(homepageHooks, "useGetActivityRequests").mockReturnValue({
    data: {},
    isLoading: false,
    refetch: vi.fn(),
    isRefetching: false
  } as any);

  const { container } = render(
    <ActivityDiagrams envs={envs.data} />
  );
  expect(container).toBeInTheDocument();
});

test("ActivityDiagrams test with no data", () => {
  const envs = {
    data: [],
  };

  const { container } = render(
    <ActivityDiagrams envs={envs.data} />
  );
  expect(container).toBeInTheDocument();
});