import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import * as pushHistoryHook from '@/hooks/pushApiEvent'
import PushHistoryList from '@/pages/EnvironmentActivityLog/components/PushHistoryList';

test("PushHistoryList", () => {
  vi.spyOn(pushHistoryHook, "useGetPushActivityLogHistory").mockReturnValue({
    data: {
      data: [
        {
          id: "9c36802a-0118-40a3-8546-b880dc6f7d15",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE"
      }],
      total: 1,
      size: 10,
      page: 0,
    },
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any)

  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <PushHistoryList handleHistoryActivityClick={vi.fn()}/>
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
