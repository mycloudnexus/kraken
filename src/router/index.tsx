import BasicLayout from "@/components/Layout/BasicLayout";
import APIServerEditSelection from "@/pages/APIServerEditSelection";
import APIServerList from "@/pages/APIServerList";
import EnvironmentActivityLog from "@/pages/EnvironmentActivityLog";
import EnvironmentOverview from "@/pages/EnvironmentOverview";
import HomePage from "@/pages/HomePage";
import NewAPIMapping from "@/pages/NewAPIMapping";
import NewAPIServer from "@/pages/NewAPIServer";
import StandardAPIMapping from "@/pages/StandardAPIMapping";
import { createBrowserRouter } from "react-router-dom";

const router = createBrowserRouter([
  {
    path: "/",
    element: <BasicLayout />,
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
          {
            path: "new",
            element: <NewAPIMapping />,
          },
        ],
      },
    ],
  },
]);

export default router;
