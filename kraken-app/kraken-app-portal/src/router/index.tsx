import AuthLayout from "@/components/Layout/AuthLayout";
import BasicLayout from "@/components/Layout/BasicLayout";
import APIServerList from "@/pages/APIServerList";
import AuditLog from "@/pages/AuditLog";
import Buyer from "@/pages/Buyer";
import EnvironmentActivityLog from "@/pages/EnvironmentActivityLog";
import EnvironmentOverview from "@/pages/EnvironmentOverview";
import HomePage from "@/pages/HomePage";
import ApiComponents from "@/pages/HomePage/components/ApiComponents";
import NewAPIServer from "@/pages/NewAPIServer";
import StandardAPIMapping from "@/pages/StandardAPIMapping";
import StandardAPIMappingTable from "@/pages/StandardAPIMappingTable";
import UserManagement from "@/pages/UserManagement";
import { lazy } from "react";
import { createBrowserRouter } from "react-router-dom";

const Login = lazy(() => import("@/pages/Login"));
const MappingTemplate = lazy(() => import("@/pages/MappingTemplate"));
const UpgradePlane = lazy(
  () => import("@/pages/MappingTemplate/DataPlaneUpgrade")
);

const router = createBrowserRouter([
  {
    path: "/login",
    element: <Login />,
  },
  {
    path: "",
    element: (
      <AuthLayout>
        <BasicLayout />
      </AuthLayout>
    ),
    children: [
      {
        path: "",
        element: <HomePage />,
      },
      {
        path: "component",
        children: [
          {
            path: ":id",
            children: [
              {
                path: "new",
                element: <NewAPIServer />,
              },
              {
                path: "list",
                element: <APIServerList />,
              },
              {
                path: "edit/:componentId",
                children: [{ path: "", element: <NewAPIServer /> }],
              },
            ],
          },
        ],
      },
      {
        path: "components",
        element: <ApiComponents />,
      },
      {
        path: "env",
        children: [
          {
            path: "",
            element: <EnvironmentOverview />,
          },
          {
            path: ":envId",
            element: <EnvironmentActivityLog />,
          },
        ],
      },
      {
        path: "api-mapping/:componentId",
        children: [
          {
            path: "",
            element: <StandardAPIMappingTable />,
          },
        ],
      },
      {
        path: "api-mapping/:componentId/:targetKey",
        children: [
          {
            path: "",
            element: <StandardAPIMapping />,
          },
        ],
      },
      {
        path: "buyer",
        element: <Buyer />,
      },
      {
        path: "audit-log",
        element: <AuditLog />,
      },
      {
        path: "mapping-template",
        children: [
          {
            path: "",
            element: <MappingTemplate />,
          },
          {
            path: "upgrade/:templateUpgradeId",
            element: <UpgradePlane />,
          },
        ],
      },
      {
        path: "user-management",
        element: <UserManagement />,
      },
    ],
  },
]);

export default router;
