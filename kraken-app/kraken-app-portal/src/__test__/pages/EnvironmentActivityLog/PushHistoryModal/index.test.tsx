import { render, waitFor } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import PushHistoryModal from '@/pages/EnvironmentActivityLog/components/PushHistoryModal';

test("PushHistoryModal", () => {
  const { container, findAllByText } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <PushHistoryModal
          isOpen={true}
          onClose={vi.fn()}
          envOptions={[{
            value: "testEnv",
            label: "Test Env"
          }]}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
  const envSelect = findAllByText("Test Env");
  waitFor(() => {
    expect(envSelect).toBeInTheDocument();
  })
});
