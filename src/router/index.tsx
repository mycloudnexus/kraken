import AuthLayout from "@/components/Layout/AuthLayout";
import BasicLayout from "@/components/Layout/BasicLayout";
import APIServerEditSelection from "@/pages/APIServerEditSelection";
import APIServerList from "@/pages/APIServerList";
import Buyer from "@/pages/Buyer";
import AuditLog from "@/pages/AuditLog";
import EnvironmentActivityLog from "@/pages/EnvironmentActivityLog";
import EnvironmentOverview from "@/pages/EnvironmentOverview";
import HomePage from "@/pages/HomePage";
import ApiComponents from '@/pages/HomePage/components/ApiComponents';
import Login from "@/pages/Login";
import NewAPIServer from "@/pages/NewAPIServer";
import StandardAPIMapping from "@/pages/StandardAPIMapping";
import { createBrowserRouter } from "react-router-dom";

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
        element: <ApiComponents />
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
        element: <AuditLog />
      }
    ],
  },
]);

export default router;
