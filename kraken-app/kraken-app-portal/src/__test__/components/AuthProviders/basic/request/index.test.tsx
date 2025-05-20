import * as requestAPI from "@/components/AuthProviders/basic/components/utils/request";
import * as refreshAPI from "@/components/AuthProviders/basic/helper/refresh";
import { refreshTokenFnc, refreshTokenWithConfig, updateToken } from "@/components/AuthProviders/basic/helper/request";
import { BasicAuthProvider, useBasicAuth } from "@/components/AuthProviders/basic/provider/BasicAuthProvider";
import { queryClient } from "@/utils/helpers/reactQuery";
import { clearData, storeData } from "@/utils/helpers/token";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render } from "@testing-library/react";
import { Button, ConfigProvider } from "antd";
import { describe, it, vi } from "vitest";
import * as callAPI from "@/components/AuthProviders/basic/helper/request";
import * as userApis from '@/services/user';

describe("Basic Authentication Request Test", () => {

  it("no stored token", async () => {
    clearData("token");
    refreshTokenFnc();
    refreshTokenWithConfig({});
  });

  it("token expired", async () => {
    const refreshAPISpy = vi.spyOn(refreshAPI, "refresh").mockResolvedValue({
      data: {
        data: {
          expiresIn: Date.now(),
          refreshTokenExpiresIn: Date.now(),
          accessToken: "a",
          refreshToken: "b"
        }
      }
    } as any);
    vi.spyOn(callAPI, "get").mockResolvedValue({
      data: {}
    } as any);

    storeData("token", "a")
    storeData("refreshToken", "a")
    storeData("refreshTokenExpiresIn", "" + (Date.now() + 3600 * 1000 * 24))
    refreshTokenFnc();

    storeData("token", "a")
    storeData("refreshToken", "a")
    storeData("refreshTokenExpiresIn", "" + (Date.now() + 3600 * 1000 * 24))
    refreshTokenWithConfig({});
    expect(refreshAPISpy).toBeCalled();
  });

  const TestingComponent = () => {
    const { getAccessToken } = useBasicAuth();
    const testUpdateToken = () => {
      window.portalConfig.getAccessToken = getAccessToken;
      updateToken({
        headers: {
          Authorization: ""
        }});
      window.portalConfig.getAccessToken = undefined;
    }

    return (
      <>
          <Button
              type="link"
              data-testId="testUpdateToken"
              onClick={testUpdateToken}
            >
          </Button>
      </>
    );
  };

  it("update token to request header", async () => {
    storeData("token", "a")
    storeData("refreshToken", "a")
    storeData("refreshTokenExpiresIn", "" + (Date.now() + 3600 * 1000 * 24))

    mockRequest()

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

    const btnUpdateToken = getByTestId("testUpdateToken");
    expect(btnUpdateToken).toBeInTheDocument();
    fireEvent.click(btnUpdateToken);
  });

  it("update token to request header when expired", async () => {
    storeData("token", "a")
    storeData("refreshToken", "a")
    storeData("tokenExpired", "" + (Date.now() - 3600 * 1000 * 24))
    storeData("refreshTokenExpiresIn", "" + (Date.now() + 3600 * 1000 * 24))

    mockRequest()

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

    const btnUpdateToken = getByTestId("testUpdateToken");
    expect(btnUpdateToken).toBeInTheDocument();
    fireEvent.click(btnUpdateToken);
  });

  const mockRequest = () => {
    vi.spyOn(requestAPI, "requestToken").mockResolvedValue({
      data: {
        data: {
          expiresIn: Date.now(),
          refreshTokenExpiresIn: Date.now(),
          accessToken: "a",
          refreshToken: "b"
        }
      }
    } as any);

    vi.spyOn(userApis, "getCurrentUser").mockReturnValue({
          name: "user1",
          email: "user1@test.com"
        });

    vi.spyOn(callAPI, "get").mockResolvedValue({
      data: {}
    } as any);

  }
});
  