import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import PreviewAPIServer from "../components/PreviewAPIServer";
import { form } from "@/utils/helpers/test";

test("PreviewAPIServer new", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <PreviewAPIServer form={form} active={true} handleBack={vi.fn()} />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
