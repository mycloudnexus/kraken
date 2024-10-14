import { fireEvent, render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import Login from "@/pages/Login";

describe("test login", () => {
  test("Login page", () => {
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Login />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
  });

  test("Login page fail", async () => {
    vi.mock("@/hooks/login", async () => {
      const actual = await vi.importActual("@/hooks/login");
      return {
        ...actual,
        useLogin: vi.fn().mockResolvedValue({
          mutateAsync: vi
            .fn()
            .mockRejectedValue(new Error({ reason: "abc" } as any)),
          isLoading: false,
        }),
      };
    });
    const { getByTestId, getByPlaceholderText } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Login />
        </BrowserRouter>
      </QueryClientProvider>
    );
    const userInput = getByPlaceholderText("User Name");
    const passwordInput = getByPlaceholderText("Password");
    fireEvent.change(userInput, { target: { value: "admin" } });
    fireEvent.change(passwordInput, { target: { value: "admin" } });
    const btnLogin = getByTestId("btn-login");
    expect(btnLogin).toBeInTheDocument();
    fireEvent.click(btnLogin);
  });
});
