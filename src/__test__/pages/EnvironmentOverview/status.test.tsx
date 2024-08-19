import { render } from "@testing-library/react";
import { queryClient } from "@/utils/helpers/reactQuery";
import EnvironmentStatus from "@/pages/EnvironmentOverview/components/EnvStatus";
import { QueryClientProvider } from '@tanstack/react-query';
import { IEnv } from '@/utils/types/env.type';

describe(" EnvironmentOverview status  component", () => {
  it("EnvironmentOverview status  component", () => {
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <EnvironmentStatus env={{ id: "1", name: "2" } as IEnv} />
      </QueryClientProvider>);
    expect(container).toBeInTheDocument();
  });
});
