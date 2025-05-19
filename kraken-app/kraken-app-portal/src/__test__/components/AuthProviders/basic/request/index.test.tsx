import * as refreshAPI from "@/components/AuthProviders/basic/helper/refresh";
import { refreshTokenFnc, refreshTokenWithConfig } from "@/components/AuthProviders/basic/helper/request";
import { clearData, storeData } from "@/utils/helpers/token";
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
});
  