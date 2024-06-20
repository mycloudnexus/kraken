import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";

import ExampleValueModal from "..";

test("test example popup", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ExampleValueModal
          method={"POST"}
          attribute={"ABC"}
          isOpen={true}
          onClose={vi.fn()}
          onOK={vi.fn()}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
