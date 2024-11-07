import { fireEvent, getAllByTestId, render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import NewAPIServer from "..";
import SelectAPIServer from '../components/SelectAPIServer';

test("test API new", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <NewAPIServer />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});


test("test SelectApiServer", async () => {
  vi.mock("@/hooks/product", async () => {
    const actual = await vi.importActual("@/hooks/product");
    return {
      ...actual,
      useGetValidateServerName: vi.fn().mockResolvedValue({
        mutateAsync: vi.fn().mockReturnValue({
          data: false
        }),
        isLoading: false,
      }),
    };
  });

  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <SelectAPIServer />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
  const input = getAllByTestId(container, "api-seller-name-input")[0];
  const formContainer = getAllByTestId(container, "api-seller-name-container")[0];
  fireEvent.change(input, { target: { value: "test" } });
  expect(formContainer).toBeInTheDocument();
});
