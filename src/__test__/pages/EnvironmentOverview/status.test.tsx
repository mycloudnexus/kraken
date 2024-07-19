import { render } from "@testing-library/react";

import EnvironmentStatus from "@/pages/EnvironmentOverview/components/EnvStatus";

describe(" EnvironmentOverview status  component", () => {
  it("EnvironmentOverview status  component", () => {
    const { container } = render(<EnvironmentStatus />);
    expect(container).toBeInTheDocument();
  });
});
