import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import NewAPIMapping from "@/pages/NewAPIMapping";

test("NewAPIMapping page", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <NewAPIMapping />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
