import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import UpgradeHistoryDetail from "@/pages/MappingTemplate/components/UpgradeHistoryDetail";

test("UpgradeHistoryDetail test", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <UpgradeHistoryDetail onClose={vi.fn()} id={"1"} noteId={"1"} open />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
