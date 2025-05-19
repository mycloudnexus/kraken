import { getData } from "@/utils/helpers/token";
import axios from "axios";


export const refresh = async (refreshToken: string) => {
  axios.post(
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