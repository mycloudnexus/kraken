import PushHistoryDrawer from "@/pages/EnvironmentActivityLog/components/PushHistoryDrawer";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

test("PushHistoryDrawer", () => {
  const { container, findAllByText } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <PushHistoryDrawer
          isOpen={true}
          onClose={vi.fn()}
          envOptions={[
            {
              value: "testEnv",
              label: "Test Env",
            },
          ]}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
  const envSelect = findAllByText("Test Env");
  waitFor(() => {
    expect(envSelect).toBeInTheDocument();
  });
});
