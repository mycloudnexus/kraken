import { Outlet } from "react-router-dom";
import Header from "../Header";

const BasicLayout = () => {
  return (
    <div>
      <Header />
      <Outlet />
    </div>
  );
};

export default BasicLayout;
