import useUser from "@/hooks/user/useUser";
import { ROUTES } from "@/utils/constants/route";
import { clearData, getData, isTokenExpired } from "@/utils/helpers/token";
import { useEffect } from "react";

const AuthLayout = ({ children }: any) => {
  const { runUser } = useUser();
  useEffect(() => {
    runUser();
  }, []);
  const token = getData("token");
  if (!token || isTokenExpired()) {
    clearData("token");
    clearData("tokenExpired");
    window.location.href = `${window.location.origin}${ROUTES.LOGIN}`;
  }
  return children;
};

export default AuthLayout;
