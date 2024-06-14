import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import StandardAPIMapping from "@/pages/StandardAPIMapping";

test("StandardAPIMapping page", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMapping />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
