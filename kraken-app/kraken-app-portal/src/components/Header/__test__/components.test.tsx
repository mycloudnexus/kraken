import { render, renderHook } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import { useTutorialStore } from '@/stores/tutorial.store';
import Header from '..';

test("test Header", () => {
  beforeAll(() => {
    const { result } = renderHook(() => useTutorialStore());
    result.current.setOpenTutorial(false)
    result.current.setTutorialCompleted(false)
  });
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
