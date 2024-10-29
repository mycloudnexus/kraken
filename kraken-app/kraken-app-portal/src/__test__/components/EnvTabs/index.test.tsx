import { render } from "@testing-library/react";
import EnvTabs from "@/components/EnvTabs";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";

test(`EnvTabs component`, async () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <EnvTabs value={"bc"} onChange={vi.fn()} />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
