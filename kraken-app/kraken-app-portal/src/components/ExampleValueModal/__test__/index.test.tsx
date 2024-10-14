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
          attribute={"ABC"}
          isOpen={true}
          onClose={vi.fn()}
          onOK={vi.fn()}
          location={"PATH"}
        />
        <ExampleValueModal
          attribute={"CDE"}
          isOpen={true}
          onClose={vi.fn()}
          onOK={vi.fn()}
          location={"QUERY"}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
