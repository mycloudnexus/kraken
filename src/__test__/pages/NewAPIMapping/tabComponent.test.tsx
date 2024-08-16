import DeployHistory, {
  ContentTime,
} from "@/pages/NewAPIMapping/components/DeployHistory";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import dayjs from "dayjs";
import { BrowserRouter } from "react-router-dom";

describe("tab", () => {
  test("deployment", () => {
    const { container } = render(
      <BrowserRouter>
        <QueryClientProvider client={queryClient}>
          <DeployHistory />
        </QueryClientProvider>
      </BrowserRouter>
    );
    expect(container).toBeInTheDocument();
  });

  test("ContentTime", () => {
    const { container } = render(
      <BrowserRouter>
        <QueryClientProvider client={queryClient}>
          <ContentTime content="123" time={dayjs().format()} />
        </QueryClientProvider>
      </BrowserRouter>
    );
    expect(container).toBeInTheDocument();
  });
});
