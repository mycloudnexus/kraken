import { ROUTES } from "@/utils/constants/route";
import { clearData, getData, isTokenExpired } from "@/utils/helpers/token";

const AuthLayout = ({ children }: any) => {
  const token = getData("token");
  if (!token || isTokenExpired()) {
    clearData("token");
    clearData("tokenExpired");
    window.location.href = `${window.location.origin}${ROUTES.LOGIN}`;
  }
  return children;
};

export default AuthLayout;
