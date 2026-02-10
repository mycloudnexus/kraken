import { UserManagement } from "@/components/AuthProviders/common/UserManagement";
import { ENV } from "@/constants";
import * as userHooks from "@/hooks/user";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { vi } from "vitest";
vi.mock("antd", async () => {
  const actual = await vi.importActual<any>("antd");
  return {
    ...actual,
    Select: ({ children, ...props }: any) => (
      <div data-testid="mock-antd-select" {...props}>
        {children}
      </div>
    ),
    Table: ({ dataSource, columns}: any) => {
      return (
        <div data-testid="mock-table">
          {dataSource?.map((record: any, index: number) => (
            <div key={record.id || index} className="mock-row">
              {columns.map((col: any, colIndex: number) => {
                const cellValue = col.dataIndex ? record[col.dataIndex] : record;
                const content = col.render
                  ? col.render(cellValue, record, index)
                  : cellValue;
                return (
                  <div key={col.key || col.dataIndex || colIndex}>{content}</div>
                );
              })}
            </div>
          ))}
        </div>
      );
    },
    Switch: (props: any) => (
      <button
        data-testid={props["data-testid"]}
        onClick={() => props.onChange?.(!props.checked)}
      >
        {props.checked ? "On" : "Off"}
      </button>
    ),
  };
});

ENV.AUTHENTICATION_TYPE = "basic";

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
