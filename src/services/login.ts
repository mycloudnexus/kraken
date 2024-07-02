import request from "@/utils/helpers/request";

export const login = (data: unknown) =>
  request("/login", {
    method: "POST",
    data,
  });
