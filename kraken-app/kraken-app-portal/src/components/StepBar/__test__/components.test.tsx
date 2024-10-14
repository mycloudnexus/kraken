import { render, renderHook } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import StepBar from "..";
import { EStep } from "@/utils/constants/common";
import { useTutorialStore } from '@/stores/tutorial.store';

test("test API step bar", () => {
  beforeAll(() => {
    const { result } = renderHook(() => useTutorialStore());
    result.current.setOpenTutorial(false)
    result.current.setTutorialCompleted(false)
  });
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StepBar
          currentStep={1}
          activeKey={"3"}
          setActiveKey={vi.fn()}
          type={EStep.API_SERVER}
        />
        <StepBar
          currentStep={1}
          activeKey={"3"}
          setActiveKey={vi.fn()}
          type={EStep.MAPPING}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
