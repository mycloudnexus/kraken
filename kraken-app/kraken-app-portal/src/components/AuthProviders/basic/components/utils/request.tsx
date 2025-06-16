import axios from "axios";
import { getData } from '@/utils/helpers/token';

export const requestToken = async (refreshToken: string) : Promise<any> => {
  const res = await axios.post(
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
  return res;
}