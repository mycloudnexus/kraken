import { render } from "@testing-library/react";
import ProductActionType from "@/components/ProductActionType";

test(`ProductActionType component`, async () => {
  const { container, getByText } = render(
    <>
      <ProductActionType productType="uni" actionType="add" />
      <ProductActionType productType="access_e_line" actionType="post" />
      <ProductActionType productType="abc" actionType="abc" />
    </>
  );
  expect(container).toBeInTheDocument();
  const uni = getByText("UNI");
  expect(uni).toBeInTheDocument();
});
