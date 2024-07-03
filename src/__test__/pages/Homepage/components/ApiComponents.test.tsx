import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import ApiComponents from "@/pages/HomePage/components/ApiComponents";
import ApiComponent from "@/pages/HomePage/components/ApiComponent";

test("ApiComponents test", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ApiComponents />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("ApiComponent test", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ApiComponent
          targetSpec={{ metadata: { logo: "http://localhost:5176/i.img" } }}
          targetYaml={{
            info: {
              description: "abc",
            },
          }}
          supportInfo={[["ADD", "DELETE"]]}
          componentList={{
            data: [],
          }}
          apis={0}
          title={"abc"}
          index={0}
          item={
            {
              metadata: {
                labels: ["a", "b", "c"],
              },
            } as any
          }
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
