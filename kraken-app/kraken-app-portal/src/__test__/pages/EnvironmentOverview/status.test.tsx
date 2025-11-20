import { render } from "@testing-library/react";
import { queryClient } from "@/utils/helpers/reactQuery";
import EnvironmentStatus from "@/pages/EnvironmentOverview/components/EnvStatus";
import { QueryClientProvider } from '@tanstack/react-query';
import { IEnv } from '@/utils/types/env.type';
import * as productHooks from "@/hooks/product";

describe(" EnvironmentOverview status  component", () => {
  it("EnvironmentOverview status  component", () => {
    vi.spyOn(productHooks, "useGetAPIDeploymentStatus").mockReturnValue({
          status: "Success",
        } as any);

    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <EnvironmentStatus env={{ id: "1", name: "2" } as IEnv} />
      </QueryClientProvider>);
    expect(container).toBeInTheDocument();
  });
});
