import NoAPIKey from "@/pages/EnvironmentOverview/components/NoAPIKey";
import { queryClient } from "@/utils/helpers/reactQuery";
import { IEnv } from "@/utils/types/env.type";
import { QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

describe(" EnvironmentOverview NoAPIKey  component", () => {
  it("EnvironmentOverview NoAPIKey  component", () => {
    const { container } = render(
      <BrowserRouter>
        <QueryClientProvider client={queryClient}>
          <NoAPIKey env={{ id: "1", name: "2" } as IEnv} />
        </QueryClientProvider>
      </BrowserRouter>
    );
    expect(container).toBeInTheDocument();
  });
});
