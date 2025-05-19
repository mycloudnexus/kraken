import * as requestAPI from "@/components/AuthProviders/basic/components/utils/request";
import * as refreshAPI from "@/components/AuthProviders/basic/helper/refresh";
import request, { handleResponseError, refreshTokenFnc, refreshTokenWithConfig } from "@/components/AuthProviders/basic/helper/request";
import { BasicAuthProvider, useBasicAuth } from "@/components/AuthProviders/basic/provider/BasicAuthProvider";
import { queryClient } from "@/utils/helpers/reactQuery";
import { clearData, storeData } from "@/utils/helpers/token";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render } from "@testing-library/react";
import { Button, ConfigProvider } from "antd";
import { AxiosError, AxiosHeaders } from "axios";
import { useEffect } from "react";
import { describe, it, vi } from "vitest";

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
    useEffect(() => {
      window.portalConfig.getAccessToken = getAccessToken;
    })

    const testUpdateToken = () => {
      try {
        request(`/products/1/productTypes`, {
          method: "GET",
        });
      } catch (e) {
        console.log(e)
      }
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

  it("handle response error", async () => {
    handleResponseError(createError(400, "Bad Request"))
    handleResponseError(createError(401, "Unauthorized"))
    handleResponseError(createError(403, "Forbidden"))
    handleResponseError(createError(404, "Not Found"))
    handleResponseError(createError(405, "Not Supported"))
    handleResponseError(createError(500, "Internal Error"))
  });

  const createError = (status: number, error: string) => {
    var request = { path: "/" };
    const headers = new AxiosHeaders();
    const config = {
      url: "http://localhost:3000",
      headers
    };
    return new AxiosError("err", "ERR", config, request, {
      status: status,
      data: { },
      statusText: error,
      config,
      headers
    });
  }
});
  