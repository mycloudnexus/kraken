import AuthLayout from "@/components/Layout/AuthLayout";
import BasicLayout from "@/components/Layout/BasicLayout";
import APIServerEditSelection from "@/pages/APIServerEditSelection";
import APIServerList from "@/pages/APIServerList";
import AuditLog from "@/pages/AuditLog";
import Buyer from "@/pages/Buyer";
import EnvironmentActivityLog from "@/pages/EnvironmentActivityLog";
import EnvironmentOverview from "@/pages/EnvironmentOverview";
import HomePage from "@/pages/HomePage";
import ApiComponents from "@/pages/HomePage/components/ApiComponents";
import MappingTemplate from "@/pages/MappingTemplate";
import NewAPIServer from "@/pages/NewAPIServer";
import StandardAPIMapping from "@/pages/StandardAPIMapping";
import UserManagement from "@/pages/UserManagement";
import { lazy } from "react";
import { createBrowserRouter } from "react-router-dom";

const Login = lazy(() => import("@/pages/Login"));
const MappingTemplateV2 = lazy(() => import("@/pages/MappingTemplatev2"));
const UpgradePlane = lazy(
  () => import("@/pages/MappingTemplatev2/UpgradePlane")
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
                children: [
                  { path: "", element: <NewAPIServer /> },
                  {
                    path: "api",
                    element: <APIServerEditSelection />,
                  },
                ],
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
        element: <MappingTemplate />,
      },
      {
        path: "mapping-template-v2",
        children: [
          {
            path: "",
            element: <MappingTemplateV2 />,
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
