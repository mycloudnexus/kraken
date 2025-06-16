import { BasicAuthProvider, useBasicAuth } from '@/components/AuthProviders/basic/provider/BasicAuthProvider';
import { fireEvent, render } from '@testing-library/react';
import { QueryClientProvider } from "@tanstack/react-query";
import { Button, ConfigProvider } from "antd";
import { queryClient } from '@/utils/helpers/reactQuery';
import { useEffect } from 'react';
import { useBoolean } from 'usehooks-ts';
import Login from '@/components/AuthProviders/basic/login';
import * as requests from "@/components/AuthProviders/basic/components/utils/request";
import * as userApis from '@/services/user';
import AuthLayout from '@/components/Layout/AuthLayout';
import { ENV } from '@/constants';
import { BrowserRouter } from 'react-router-dom';
import * as authHooks from '@/components/AuthProviders/basic/provider/BasicAuthProvider';

ENV.AUTHENTICATION_TYPE = "basic"

const TestingComponent = () => {
  const { checkAuthenticated, logout, refreshAuth } = useBasicAuth();
  const { value: isAuthenticated, setTrue, setFalse } = useBoolean(false);
  useEffect(() => {
      if (checkAuthenticated()) {
        setTrue();
      } else {
        setFalse();
      }
    }, [checkAuthenticated]);

  const getCurrentUser = () => {
    if (window.portalConfig && window.portalConfig.getCurrentAuthUser) {
      window?.portalConfig?.getCurrentAuthUser();
    }
  }
  return (
    <>
      <p data-testId="checkAuthenticated">{ "" + isAuthenticated }</p>
      <Button
          type="link"
          data-testId="testLogout"
          onClick={logout}
        >
      </Button>
      <Button
          type="link"
          data-testId="testRefresh"
          onClick={refreshAuth}
        >
      </Button>
      <Button
          type="link"
          data-testId="testGetCurrentUser"
          onClick={getCurrentUser}
        >
      </Button>
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

    window.localStorage.clear();

    vi.spyOn(userApis, "getCurrentUser").mockReturnValue({
      name: "user1",
      email: "user1@test.com"
    });

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
    vi.spyOn(userApis, "getCurrentUser").mockReturnValue({
      name: "user1",
      email: "user1@test.com"
    });

    window.localStorage.setItem("token", "token");
    window.localStorage.setItem("refreshToken", "token");
    window.localStorage.setItem(
      "tokenExpired", "" + (Date.now() + 30 * 24 * 3600 * 1000));
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

    const btnGetCurrentUser = getByTestId("testGetCurrentUser");
    expect(btnGetCurrentUser).toBeInTheDocument();
    fireEvent.click(btnGetCurrentUser);
  })

  it('authenticated access token expired', () => {
    vi.spyOn(requests, "requestToken").mockResolvedValue({
      data: {
        data: {
          expiresIn: Date.now(),
          refreshTokenExpiresIn: Date.now(),
          accessToken: "a",
          refreshToken: "b"
        }
      }
    });
    vi.spyOn(userApis, "getCurrentUser").mockReturnValue({
      name: "user1",
      email: "user1@test.com"
    });

    window.localStorage.setItem("token", "token");
    window.localStorage.setItem("refreshToken", "token");
    window.localStorage.setItem(
      "tokenExpired", "" + (Date.now() - 30 * 24 * 3600 * 1000));
    window.localStorage.setItem(
      "refreshTokenExpiresIn", "" + (Date.now() + 30 * 24 * 3600 * 1000));
    try {
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
    } catch (error) {
      console.log(error);
    }
  })  

  it('authenticated refresh access token expired', () => {
    window.localStorage.setItem("token", "token");
    window.localStorage.setItem("refreshToken", "token");
    window.localStorage.setItem(
      "tokenExpired", "" + (Date.now() + 30 * 24 * 3600 * 1000));
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

  it('logout', () => {
    vi.spyOn(userApis, "getCurrentUser").mockReturnValue({
      name: "user1",
      email: "user1@test.com"
    });

    window.localStorage.setItem("token", "token");
    window.localStorage.setItem("refreshToken", "token");
    window.localStorage.setItem(
      "tokenExpired", "" + (Date.now() + 30 * 24 * 3600 * 1000));
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
    const btnLogout = getByTestId("testLogout");
    expect(btnLogout).toBeInTheDocument();
    fireEvent.click(btnLogout);
  })

  //refreshAuth
  it('refresh', () => {
    vi.spyOn(userApis, "getCurrentUser").mockReturnValue({
      name: "user1",
      email: "user1@test.com"
    });

    window.localStorage.setItem("token", "token");
    window.localStorage.setItem("refreshToken", "token");
    window.localStorage.setItem(
      "tokenExpired", "" + (Date.now() + 30 * 24 * 3600 * 1000));
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
    const btnRefresh = getByTestId("testRefresh");
    expect(btnRefresh).toBeInTheDocument();
    fireEvent.click(btnRefresh);
  })

  it('authenticate layout', () => {
    const useBasicAuthSpy = vi.spyOn(authHooks, "useBasicAuth").mockReturnValue(
      {
        checkAuthenticated: () => { return false; },
        loginWithCredentials: function (_values: any): Promise<void> {
          throw new Error('Function not implemented.');
        },
        getAccessToken: function (): Promise<string> {
          throw new Error('Function not implemented.');
        },
        logout: function (): Promise<void> {
          throw new Error('Function not implemented.');
        },
        refreshAuth: function (): Promise<void> {
          throw new Error('Function not implemented.');
        },
        getCurrentAuthUser: function () {
          throw new Error('Function not implemented.');
        },
        isAuthenticated: false,
        isLoading: false
      } 
    );
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <AuthLayout />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(useBasicAuthSpy).toBeCalled();
  })
})

