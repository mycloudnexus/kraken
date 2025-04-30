import { UserManagement } from "@/components/AuthProviders/common/UserManagement";
import * as userHooks from "@/hooks/user";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

test("UserManagement test", () => {
  vi.spyOn(userHooks, "useGetUserList").mockReturnValue({
    data: {
      data: [
        {
          createdAt: "test-createdat-timestamp",
          createdBy: "test-createdby-id",
          email: "test-email.com",
          id: "test-id",
          name: "test-name",
          role: "test-role",
          state: "ENABLED",
        },
      ],
      total: 1,
      page: 0,
      size: 20,
    },
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any);
  const { container, getByTestId } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <UserManagement />
      </BrowserRouter>
    </QueryClientProvider>
  );
  const tableSwitch = getByTestId("switch-0");
  fireEvent.click(tableSwitch);
  expect(container).toBeInTheDocument();
});
