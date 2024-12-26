import { render, } from "@testing-library/react";
import renderRequiredMark from '../index';

test("test RequiredMark component without required flag", () => {
  const label = 'Name';
  const { container } = render(
    renderRequiredMark(label, { required: false }) as React.ReactElement
  );

  expect(container).toHaveTextContent(label);
  expect(container).toHaveTextContent('Name');
});

test("test RequiredMark component with required flag", () => {
  const label = 'Name';
  const { container } = render(
    renderRequiredMark(label, { required: true }) as React.ReactElement
  );

  expect(container).toHaveTextContent(label);
  expect(container).toHaveTextContent('Name *');
});
