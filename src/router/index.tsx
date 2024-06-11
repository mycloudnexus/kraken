import BasicLayout from "@/components/Layout/BasicLayout";
import APIServerList from "@/pages/APIServerList";
import EnvironmentActivityLog from "@/pages/EnvironmentActivityLog";
import EnvironmentOverview from "@/pages/EnvironmentOverview";
import HomePage from "@/pages/HomePage";
import NewAPIServer from "@/pages/NewAPIServer";
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
        path: "component/:id",
        children: [
          {
            path: "new",
            element: <NewAPIServer />,
          },
          {
            path: "list",
            element: <APIServerList />,
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
    ],
  },
]);

export default router;
