import "./App.scss";
import { RouterProvider } from "react-router-dom";
import router from "./router";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "./utils/helpers/reactQuery";
import { ConfigProvider } from "antd";

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ConfigProvider
        button={{ style: { borderRadius: 4 } }}
        input={{ style: { borderRadius: 4 } }}
      >
        <RouterProvider router={router} />
      </ConfigProvider>
    </QueryClientProvider>
  );
}

export default App;
