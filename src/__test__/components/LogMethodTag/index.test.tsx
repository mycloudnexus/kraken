import { render, screen } from "@testing-library/react";
import LogMethodTag from "@/components/LogMethodTag";

["GET", "POST", "PUT", "PATCH", "DELETE"].map((method) =>
  test(`LogMethodTag components method ${method}`, async () => {
    render(<LogMethodTag method={method} />);
    const element = await screen.findByText(method);
    expect(element).toBeInTheDocument();
  })
);
