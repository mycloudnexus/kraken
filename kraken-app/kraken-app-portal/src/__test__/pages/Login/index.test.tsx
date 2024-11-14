import { fireEvent, render } from "@/__test__/utils";
import Login from "@/pages/Login";

describe("test login", () => {
  test("Login page", async () => {
    vi.mock("@/hooks/login", async () => {
      const actual = await vi.importActual("@/hooks/login");
      return {
        ...actual,
        useLogin: vi.fn().mockReturnValue({
          mutateAsync: vi.fn().mockReturnValue({
            data: {
              accessToken: "a",
              expiresIn: "b",
              refreshToken: "c",
              refreshTokenExpiresIn: "d",
            },
          }),
          isLoading: false,
        }),
      };
    });
    const { getByTestId, getByPlaceholderText } = render(<Login />);
    const userInput = getByPlaceholderText("User Name");
    const passwordInput = getByPlaceholderText("Password");
    fireEvent.change(userInput, { target: { value: "admin" } });
    fireEvent.change(passwordInput, { target: { value: "admin" } });
    const btnLogin = getByTestId("btn-login");
    expect(btnLogin).toBeInTheDocument();
    fireEvent.click(btnLogin);
  });
});
