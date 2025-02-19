import APIServerCard from "@/pages/APIServerList/components/APIServerCard";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

describe("ExpandRow", () => {
  test("renders empty table when selectedAPIs is empty", () => {
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <APIServerCard item={{ facets: { selectedAPIs: [] } } as any} />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
  });

  test("renders table with data when selectedAPIs is not empty", () => {
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <APIServerCard
            item={
              {
                facets: { selectedAPIs: [{}] },
                metadata: { key: "1", name: "1", id: "1" },
              } as any
            }
          />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
  });
});
