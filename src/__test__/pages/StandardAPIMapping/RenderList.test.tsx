import { render, screen } from "@testing-library/react";
import RenderList from "@/pages/StandardAPIMapping/components/RenderList";

test("RenderList component", async () => {
  render(
    <RenderList
      data={[
        {
          path: "/a/b/c/d",
          method: "POST",
        },
      ]}
    />
  );
  const methodEle = await screen.findByText("POST");
  const pathEle = await screen.findByText("/a/b/c/d");
  expect(methodEle).toBeInTheDocument();
  expect(pathEle).toBeInTheDocument();
});
