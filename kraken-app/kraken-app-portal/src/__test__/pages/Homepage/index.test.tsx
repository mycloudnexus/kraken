import * as productHooks from "@/hooks/product";
import HomePage from "@/pages/HomePage";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

test("Hompage test", () => {
  vi.spyOn(productHooks, "useGetProductEnvs").mockReturnValue({
    data: {
      data: [
        {
          id: "32b4832f-fb2f-4c99-b89a-c5c995b18dfc",
          productId: "mef.sonata",
          createdAt: "2024-05-30T13:02:03.224486Z",
          name: "production",
        },
      ],
    },
    isLoading: false,
  } as any);

  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <HomePage />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
