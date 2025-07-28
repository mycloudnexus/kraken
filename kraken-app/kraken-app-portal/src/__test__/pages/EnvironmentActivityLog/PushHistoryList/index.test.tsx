import * as pushHistoryHook from "@/hooks/pushApiEvent";
import PushHistoryList from "@/pages/EnvironmentActivityLog/components/PushHistoryList";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import {
  fireEvent,
  render,
  screen,
  waitFor
} from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

test("PushHistoryList", () => {
  vi.spyOn(pushHistoryHook, "useGetPushActivityLogHistory").mockReturnValue({
    data: {
      data: [
        {
          id: "1",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE",
        },
        {
          id: "2",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE",
        },
        {
          id: "3",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE",
        },
        {
          id: "4",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE",
        },
        {
          id: "5",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE",
        },
        {
          id: "6",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE",
        },
        {
          id: "7",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE",
        },
        {
          id: "8",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE",
        },
        {
          id: "9",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE",
        },
        {
          id: "10",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE",
        },
        {
          id: "11",
          createdAt: "2024-11-20T08:40:38.480088Z",
          envName: "stage",
          startTime: "2024-10-09T23:00:00Z",
          endTime: "2024-10-11T22:59:59Z",
          pushedBy: "fc5f6165-aa20-4798-a9ee-78afbe3cf06d",
          status: "DONE",
        }
      ],
      total: 11,
      size: 10,
      page: 0,
    },
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any);

  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <PushHistoryList />
      </BrowserRouter>
    </QueryClientProvider>
  );
  waitFor(
      () => { expect(container).toBeInTheDocument()});
  const button = screen.getByTitle('2');
  fireEvent.click(button);
  waitFor(
      () => { expect(container).toBeInTheDocument()});
});
