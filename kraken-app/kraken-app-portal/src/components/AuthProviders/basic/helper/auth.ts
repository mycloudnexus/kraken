import { getData } from "@/utils/helpers/token";
import axios from "axios";


export const refresh = async (refreshToken: string) => {
  return await axios.post(
    import.meta.env.VITE_BASE_API + "/auth/token",
    {
      refreshToken,
      grantType: "REFRESH_TOKEN",
    },
    {
      headers: {
        Authorization: `Bearer ${getData("token")}`,
      },
    }
  );
}

export const login = async (data: unknown) => {
  return await axios.post(
    import.meta.env.VITE_BASE_API + "/login",
    data
  );
}