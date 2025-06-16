import Authenticate from "../AuthProviders/common/authenticte";
import BasicLayout from "./BasicLayout";

const AuthLayout = () => {
  return (
    <Authenticate>
      <BasicLayout />
    </Authenticate>
  );
};

export default AuthLayout;
