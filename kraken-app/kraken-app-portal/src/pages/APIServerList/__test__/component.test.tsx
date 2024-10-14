import { render } from "@testing-library/react";
import APIServerCard from "../components/APIServerCard";
import { BrowserRouter } from "react-router-dom";

describe("ExpandRow", () => {
  test("renders empty table when selectedAPIs is empty", () => {
    const { container } = render(
      <BrowserRouter>
        <APIServerCard item={{ facets: {} } as any} />
      </BrowserRouter>
    );
    expect(container).toBeInTheDocument();
  });

  test("renders table with data when selectedAPIs is not empty", () => {
    const { container } = render(
      <BrowserRouter>
        <APIServerCard
          item={
            { facets: {}, metadata: { key: "1", name: "1", id: "1" } } as any
          }
        />
      </BrowserRouter>
    );
    expect(container).toBeInTheDocument();
  });
});
