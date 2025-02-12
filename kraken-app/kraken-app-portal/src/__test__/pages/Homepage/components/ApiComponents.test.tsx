import ApiComponent from "@/pages/HomePage/components/ApiComponent";
import ApiComponents from "@/pages/HomePage/components/ApiComponents";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

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
          apis={0}
          title={"abc"}
          item={
            {
              metadata: {
                labels: ["a", "b", "c"],
              },
            } as any
          }
          openDrawer={() => {}}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("ApiComponent test with see more", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ApiComponent
          targetSpec={{ metadata: { logo: "http://localhost:5176/i.img" } }}
          targetYaml={{
            info: {
              description:
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla a ligula velit. Duis dictum vehicula finibus. Vivamus est quam, cursus ac ligula at, egestas blandit leo. Pellentesque lobortis, turpis ac feugiat tempor, massa nulla consequat justo, nec volutpat felis elit ultrices velit. Duis porttitor molestie.",
            },
          }}
          supportInfo={[["ADD", "DELETE"]]}
          apis={0}
          title={"abc"}
          item={
            {
              metadata: {
                labels: ["a", "b", "c"],
              },
            } as any
          }
          openDrawer={() => {}}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
