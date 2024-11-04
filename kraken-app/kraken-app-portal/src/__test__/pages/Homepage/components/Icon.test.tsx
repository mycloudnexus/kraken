import {
  OneIcon,
  TwoIcon,
  ThreeIcon,
  CheckFilled,
  CheckGray,
  MoreIcon,
} from "@/pages/HomePage/components/Icon";
import { render } from "@testing-library/react";

test("Hompage Icon test", () => {
  [
    <OneIcon />,
    <TwoIcon />,
    <ThreeIcon />,
    <CheckFilled />,
    <CheckGray />,
    <MoreIcon />,
  ].forEach((f) => {
    const { container } = render(f);
    expect(container).toBeInTheDocument();
  });
});
