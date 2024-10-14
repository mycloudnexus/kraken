import RenderList from "@/pages/StandardAPIMapping/components/RenderList";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

test("RenderList component", async () => {
  render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <RenderList
          data={[
            {
              path: "/a/b/c/d",
              method: "POST",
            },
          ]}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  const methodEle = await screen.findByText("POST");
  const pathEle = await screen.findByText("/a/b/c/d");
  expect(methodEle).toBeInTheDocument();
  expect(pathEle).toBeInTheDocument();
});
