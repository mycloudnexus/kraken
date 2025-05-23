import * as productHooks from "@/hooks/product";
import * as pushApiHooks from "@/hooks/pushApiEvent";
import PushHistoryDrawer from "@/pages/EnvironmentActivityLog/components/PushHistoryDrawer";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, waitFor, fireEvent } from "@testing-library/react";
import dayjs from "dayjs";
import { BrowserRouter } from "react-router-dom";

test("PushHistoryDrawer", () => {
  vi.spyOn(productHooks, 'useGetProductTypes').mockReturnValue({
            data: [
                "UNI:UNI",
                "ACCESS_E_LINE:Access Eline",
                "SHARE:Shared"
            ]
        } as any);

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
  const { container, findAllByText, getByTestId, getAllByText } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <PushHistoryDrawer
          isOpen={true}
          onClose={vi.fn()}
          envOptions={[
            {
              value: "testEnv",
              label: "stage",
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

  const datepicker = getByTestId("datePicker");
  fireEvent.click(datepicker);

  const dateToSelect = dayjs().get("date");
  const dateElement = getAllByText(dateToSelect);
  fireEvent.click(dateElement[0]);

  const okButton = getByTestId("pushLog-btn");
  fireEvent.click(okButton);
});
