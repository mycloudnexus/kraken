import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import DeployStandardAPIModal from "../DeployStandardAPIModal";

test("test Deploy popup", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <DeployStandardAPIModal open={true} onClose={vi.fn()} />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
