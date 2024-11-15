import ActivityDiagrams from '@/pages/HomePage/components/ActivityDiagrams';
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import * as homepageHooks from '@/hooks/homepage'

test("ActivityDiagrams test", () => {
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

  const { container, getByTestId } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ActivityDiagrams envs={envs.data} />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
  const recentButton = getByTestId('recent-7-days');
  fireEvent.click(recentButton);
});
