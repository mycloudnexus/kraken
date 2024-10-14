import { render } from "@testing-library/react";

import {
  Step1Icon,
  Step2Icon,
  Step3Icon,
  Step4Icon,
  RightArrow,
  MoreIcon,
} from "@/pages/HomePage/components/Icon";

test("Hompage Icon test", () => {
  [
    <Step1Icon />,
    <Step2Icon />,
    <Step3Icon />,
    <Step4Icon />,
    <RightArrow />,
    <MoreIcon />,
  ].forEach((f) => {
    const { container } = render(f);
    expect(container).toBeInTheDocument();
  });
});
