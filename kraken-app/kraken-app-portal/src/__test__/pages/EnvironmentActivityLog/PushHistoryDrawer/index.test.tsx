import * as productHooks from "@/hooks/product";
import * as pushApiHooks from "@/hooks/pushApiEvent";
import PushHistoryDrawer from "@/pages/EnvironmentActivityLog/components/PushHistoryDrawer";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, waitFor, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

test("PushHistoryDrawer", () => {
  vi.spyOn(productHooks, "useGetProductEnvActivitiesMutation").mockReturnValue({
    data: {
      data: {
        data: [{}, {}],
        total: 2,
      },
    },
    mutateAsync: vi.fn(),
  } as any);
  vi.spyOn(pushApiHooks, "usePostPushActivityLog").mockReturnValue({
    mutateAsync: vi.fn(),
  } as any);
  const { container, findAllByText, getByTestId } = render(
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
  const okButton = getByTestId("pushLog-btn");
  fireEvent.click(okButton);
});
