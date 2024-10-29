import useUser from "@/hooks/user/useUser";
import { useEffect } from "react";

const AuthLayout = ({ children }: any) => {
  const { runUser } = useUser();
  useEffect(() => {
    runUser();
  }, []);

  return children;
};

export default AuthLayout;
