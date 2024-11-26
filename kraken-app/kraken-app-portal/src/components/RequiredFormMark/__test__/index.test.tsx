import { render, waitFor } from "@testing-library/react";
import RequiredMark from "../index";

test("test RequiredMark component with required flag", () => {
  const { container, findByText } = render(
    <RequiredMark
      label={<>TEST</>}
      required={true}
    />
  );
  expect(container).toBeInTheDocument();
  const text = findByText("*");
  waitFor(() => {
    expect(text).toBeInTheDocument()
  })
});

test("test RequiredMark component with non-required flag", () => {
  const { container } = render(
    <RequiredMark
      label={<>TEST</>}
      required={false}
    />
  );
  expect(container).toBeInTheDocument();
});
