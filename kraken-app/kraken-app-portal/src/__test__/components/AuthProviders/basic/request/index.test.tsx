import * as refreshAPI from "@/components/AuthProviders/basic/helper/refresh";
import { handleResponseError, refreshTokenFnc, refreshTokenWithConfig } from "@/components/AuthProviders/basic/helper/request";
import { clearData, storeData } from "@/utils/helpers/token";
import { AxiosError, AxiosHeaders } from "axios";
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
  