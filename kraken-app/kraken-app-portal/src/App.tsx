import { QueryClientProvider } from "@tanstack/react-query";
import { ConfigProvider, notification } from "antd";
import { RouterProvider } from "react-router-dom";
import router from "./router";
import { queryClient } from "./utils/helpers/reactQuery";
import { AuthProvider } from "./components/AuthProviders/common/provider";

function App() {
  notification.config({
    duration: 3,
  });
  return (
    <QueryClientProvider client={queryClient}>
      <ConfigProvider
        input={{ style: { borderRadius: 4 } }}
        theme={{
          components: {
            Button: {
              colorPrimary: "#2962FF",
              borderRadius: 4,
            },
          },
        }}
      >
        <AuthProvider>
          <RouterProvider router={router} />
        </AuthProvider>
      </ConfigProvider>
    </QueryClientProvider>
  );
}

export default App;
