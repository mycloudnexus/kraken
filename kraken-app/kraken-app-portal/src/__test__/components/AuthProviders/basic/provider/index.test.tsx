import { BasicAuthProvider, useBasicAuth } from '@/components/AuthProviders/basic/BasicAuthProvider';
import { fireEvent, render } from '@testing-library/react';
import { QueryClientProvider } from "@tanstack/react-query";
import { ConfigProvider } from "antd";
import { queryClient } from '@/utils/helpers/reactQuery';
import { useEffect } from 'react';
import { useBoolean } from 'usehooks-ts';
import Login from '@/components/AuthProviders/basic/login';


const TestingComponent = () => {
  const { checkAuthenticated } = useBasicAuth();
  const { value: isAuthenticated, setTrue, setFalse } = useBoolean(false);
  useEffect(() => {
      if (checkAuthenticated()) {
        setTrue();
      } else {
        setFalse();
      }
    }, [checkAuthenticated])
  return (
    <>
      <p data-testId="checkAuthenticated">{ "" + isAuthenticated }</p>
    </>
  );
};

describe('Use basic auth provider', () => {
  it('anonymous access', () => {
    window.localStorage.clear();
    const { getByTestId } = render(
      <QueryClientProvider client={queryClient}>
        <ConfigProvider
          input={{ style: { borderRadius: 4 } }}
          theme={{
            components: {
              Button: {
                colorPrimary: "#2962FF",
                borderRadius: 4,
              },
            },
          }}
        >
          <BasicAuthProvider>
            <TestingComponent />
          </BasicAuthProvider>
        </ConfigProvider>
      </QueryClientProvider>
    );
    const checkAuthenticated = getByTestId('checkAuthenticated');
    expect(checkAuthenticated).toHaveTextContent("false");
  })

  it("Login page", async () => {
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

    const { getByTestId, getByPlaceholderText } = render(
      <QueryClientProvider client={queryClient}>
        <ConfigProvider
          input={{ style: { borderRadius: 4 } }}
          theme={{
            components: {
              Button: {
                colorPrimary: "#2962FF",
                borderRadius: 4,
              },
            },
          }}
        >
          <BasicAuthProvider>
            <Login />
          </BasicAuthProvider>
        </ConfigProvider>
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

  it('authenticated access', () => {
    window.localStorage.setItem("token", "token");
    window.localStorage.setItem("refreshToken", "token");
    window.localStorage.setItem(
      "refreshTokenExpiresIn", "" + (Date.now() + 30 * 24 * 3600 * 1000));
    const { getByTestId } = render(
      <QueryClientProvider client={queryClient}>
        <ConfigProvider
          input={{ style: { borderRadius: 4 } }}
          theme={{
            components: {
              Button: {
                colorPrimary: "#2962FF",
                borderRadius: 4,
              },
            },
          }}
        >
          <BasicAuthProvider>
            <TestingComponent />
          </BasicAuthProvider>
        </ConfigProvider>
      </QueryClientProvider>
    );
    const checkAuthenticated = getByTestId('checkAuthenticated');
    expect(checkAuthenticated).toHaveTextContent("true");
  })

  it('authenticated access token expired', () => {
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

    window.localStorage.setItem("token", "token");
    window.localStorage.setItem("refreshToken", "token");
    window.localStorage.setItem(
      "tokenExpired", "" + (Date.now() - 30 * 24 * 3600 * 1000));
    window.localStorage.setItem(
      "refreshTokenExpiresIn", "" + (Date.now() + 30 * 24 * 3600 * 1000));
    const { getByTestId } = render(
      <QueryClientProvider client={queryClient}>
        <ConfigProvider
          input={{ style: { borderRadius: 4 } }}
          theme={{
            components: {
              Button: {
                colorPrimary: "#2962FF",
                borderRadius: 4,
              },
            },
          }}
        >
          <BasicAuthProvider>
            <TestingComponent />
          </BasicAuthProvider>
        </ConfigProvider>
      </QueryClientProvider>
    );
    const checkAuthenticated = getByTestId('checkAuthenticated');
    expect(checkAuthenticated).toHaveTextContent("true");
  })  

  it('authenticated refresh access token expired', () => {
    window.localStorage.setItem("token", "token");
    window.localStorage.setItem("refreshToken", "token");
    window.localStorage.setItem(
      "refreshTokenExpiresIn", "" + (Date.now() - 30 * 24 * 3600 * 1000));
    const { getByTestId } = render(
      <QueryClientProvider client={queryClient}>
        <ConfigProvider
          input={{ style: { borderRadius: 4 } }}
          theme={{
            components: {
              Button: {
                colorPrimary: "#2962FF",
                borderRadius: 4,
              },
            },
          }}
        >
          <BasicAuthProvider>
            <TestingComponent />
          </BasicAuthProvider>
        </ConfigProvider>
      </QueryClientProvider>
    );
    const checkAuthenticated = getByTestId('checkAuthenticated');
    expect(checkAuthenticated).toHaveTextContent("false");
  })  
})

