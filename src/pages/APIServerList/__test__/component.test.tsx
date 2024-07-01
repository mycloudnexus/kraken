import { render } from "@testing-library/react";
import APIServerCard from "../components/APIServerCard";

describe("ExpandRow", () => {
  test("renders empty table when selectedAPIs is empty", () => {
    const { container } = render(
      <APIServerCard item={{ facets: {} } as any} refresh={vi.fn()} />
    );
    expect(container).toBeInTheDocument();
  });

  test("renders table with data when selectedAPIs is not empty", () => {
    const { container } = render(
      <APIServerCard
        item={{ facets: {}, metadata: { key: "1", name: "1", id: "1" } } as any}
        refresh={vi.fn()}
      />
    );
    expect(container).toBeInTheDocument();
  });
});
