import { render } from "@testing-library/react";
import MappingMatrix from "@/components/MappingMatrix";

test(`MappingMatrix component`, async () => {
  const { container, getByText } = render(
    <>
      <MappingMatrix mappingMatrix={{uni: "add"}} />
    </>
  );
  expect(container).toBeInTheDocument();
  const uni = getByText("uni");
  expect(uni).toBeInTheDocument();
});
