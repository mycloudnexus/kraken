import QuickStartGuide from "@/pages/HomePage/components/QuickStartGuide";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

test("StepCard test", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <QuickStartGuide />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
