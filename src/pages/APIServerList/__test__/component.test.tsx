import { render } from "@testing-library/react";
import ExpandRow from "../components/ExpandRow";

describe("ExpandRow", () => {
  test("renders empty table when selectedAPIs is empty", () => {
    const { container } = render(<ExpandRow item={{ facets: {} }} />);
    expect(container).toBeInTheDocument();
  });

  test("renders table with data when selectedAPIs is not empty", () => {
    const { container } = render(
      <ExpandRow
        item={{
          facets: { selectedAPIs: ["/api/products POST"] },
        }}
      />
    );
    expect(container).toBeInTheDocument();
  });
});
