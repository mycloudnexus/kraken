import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import SelectResponseProperty from "@/pages/NewAPIMapping/components/SelectResponseProperty";
import ResponseMapping from "@/pages/NewAPIMapping/components/ResponseMapping";

test("component new api map page", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <SelectResponseProperty />
        <ResponseMapping />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
