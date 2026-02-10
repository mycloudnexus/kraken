import * as productModule from "@/hooks/product";
import Buyer from "@/pages/Buyer";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, fireEvent, waitFor, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import * as userHooks from '@/hooks/user/useUser'
import { ENV } from "@/constants";
import { vi } from "vitest";

vi.mock("antd", async () => {
  const actual = await vi.importActual<any>("antd");
  const { cloneElement, isValidElement, Children } = await vi.importActual<any>("react");

  return {
    ...actual,
    Select: ({ children, ...props }: any) => (
      <div data-testid="mock-antd-select" {...props}>{children}</div>
    ),
    Table: ({ dataSource, columns }: any) => {
      return (
        <div data-testid="mock-table">
          {dataSource?.map((record: any, index: number) => (
            <div key={record.id || index} className="mock-row">
              {columns.map((col: any, colIndex: number) => {
                if (col.hidden) return null;
                const cellValue = col.dataIndex ? record[col.dataIndex] : record;
                const content = col.render ? col.render(cellValue, record, index) : cellValue;
                return <div key={col.key || col.dataIndex || colIndex}>{content}</div>;
              })}
            </div>
          ))}
        </div>
      );
    },

    Popconfirm: ({ children, onConfirm }: any) => {
      return Children.map(children, (child: any) => {
        if (isValidElement(child)) {
          return cloneElement(child, {
            onClick: (e: any) => {
              if (child.props.onClick) child.props.onClick(e);
              onConfirm();
            }
          } as any);
        }
        return child;
      });
    },
    Modal: ({ children, open, title }: any) => {
      if (!open) return null;
      return (
        <div data-testid="mock-modal">
          <div>{title}</div>
          {children}
        </div>
      );
    },
  };
});

const setupMocks = () => {
  ENV.VIEW_BUYER_TOKEN = 'true';

  vi.spyOn(userHooks, "useUser").mockReturnValue({
    findUserName: () => 'ADMIN',
    currentUser: { role: "ADMIN" }
  } as any);

  vi.spyOn(productModule, "useGetBuyerList").mockReturnValue({
    data: {
      data: [
        {
          kind: "kraken.product-buyer",
          id: "test-id-1",
          metadata: { id: "metadata-id-1", status: "deactivated" },
          facets: { buyerInfo: { envId: "envId", buyerId: "buyerId", companyName: "companyName" } },
          createdAt: "2024-01-01",
          createdBy: "admin",
        }
      ],
      page: 0, total: 1, size: 10,
    },
    isLoading: false,
  } as any);

  const activeBuyerMutate = vi.fn().mockResolvedValue({ data: { buyerToken: { accessToken: "active-token" } } });
  const retrieveTokenMutate = vi.fn().mockResolvedValue({ data: { accessToken: "retrieve-token" } });

  vi.spyOn(productModule, "useActiveBuyer").mockReturnValue({
    mutateAsync: activeBuyerMutate,
  } as any);

  vi.spyOn(productModule, "useRetrieveToken").mockReturnValue({
    mutateAsync: retrieveTokenMutate,
  } as any);

  return {
    activeBuyerMutate,
    retrieveTokenMutate
  };
};

test("Retrieve Token flow opens modal with token", async () => {
  const { retrieveTokenMutate } = setupMocks();

  render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Buyer />
      </BrowserRouter>
    </QueryClientProvider>
  );

  const retrieveTokenBtn = screen.getByTestId("retrieve-token-test-id-1");
  fireEvent.click(retrieveTokenBtn);

  await waitFor(() => {
    expect(retrieveTokenMutate).toHaveBeenCalled();
  });

  await waitFor(() => {
    const tokens = screen.getAllByText(/Here’s your generated token/i);
    expect(tokens[0]).toBeInTheDocument();
  });
});

test("Activate Buyer flow opens modal", async () => {
  const { activeBuyerMutate } = setupMocks();

  render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Buyer />
      </BrowserRouter>
    </QueryClientProvider>
  );

  const activateBtn = screen.getByTestId("test-id-1-activate");
  fireEvent.click(activateBtn);

  await waitFor(() => {
    expect(activeBuyerMutate).toHaveBeenCalled();
  });

  await waitFor(() => {
    const tokens = screen.getAllByText(/Here’s your generated token/i);
    expect(tokens[0]).toBeInTheDocument();
  });
});