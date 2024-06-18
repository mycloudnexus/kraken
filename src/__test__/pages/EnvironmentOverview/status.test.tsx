import { render } from "@testing-library/react";

import EnvironmentStatus from "@/pages/EnvironmentOverview/components/EnvStatus";

describe(" EnvironmentOverview status  component", () => {
  it("EnvironmentOverview status  component", () => {
    const { container } = render(<EnvironmentStatus />);
    expect(container).toBeInTheDocument();
  });
  it("status should show disconnect", async () => {
    const { getByText } = render(
      <EnvironmentStatus disConnect={10} dataPlane={10} apiKey />
    );
    expect(
      getByText(/Disconnected/i, {
        trim: false,
        collapseWhitespace: false,
      })
    ).toBeInTheDocument();
  });
});
