import APIServerList from "@/pages/APIServerList";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

describe("test API Server List tab", () => {
  test("test API server setup page", () => {
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <APIServerList />
        </BrowserRouter>
      </QueryClientProvider>
    );
    expect(container).toBeInTheDocument();
  });

  test("test Contact information setup tab", () => {
    const { container, getByText } = render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <APIServerList />
        </BrowserRouter>
      </QueryClientProvider>
    );

    const item = getByText("Contact information setup");
    fireEvent.click(item);
    expect(container).toBeInTheDocument();
  });
});
