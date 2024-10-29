import { render, renderHook } from "@/__test__/utils";
import { useTutorialStore } from '@/stores/tutorial.store';
import Header from '..';

test("test Header", () => {
  beforeAll(() => {
    const { result } = renderHook(() => useTutorialStore());
    result.current.setOpenTutorial(false)
    result.current.setTutorialCompleted(false)
  });
  const { container } = render(
    <Header />
  );
  expect(container).toBeInTheDocument();
});
