import { fireEvent, render, waitFor } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import StandardAPIMapping from "@/pages/StandardAPIMapping";

test("StandardAPIMapping page", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMapping />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("StandardAPIMapping btn-create-version", () => {
  vi.mock("@/hooks/product", async () => {
    const actual = await vi.importActual("@/hooks/product");
    return {
      ...actual,
      useCreateNewVersion: vi.fn().mockResolvedValue({
        mutateAsync: vi
          .fn()
          .mockRejectedValue(new Error({ reason: "abc" } as any)),
        isLoading: false,
      }),
    };
  });
  const { getByTestId } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMapping />
      </BrowserRouter>
    </QueryClientProvider>
  );
  const btn = getByTestId("btn-create-version");
  expect(btn).toBeInTheDocument();
  fireEvent.click(btn);
  waitFor(() => {
    const btnOk = getByTestId("btn-ok");
    expect(btnOk).toBeInTheDocument();
    fireEvent.click(btnOk);
  });
});
