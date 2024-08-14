import { fireEvent, render } from "@testing-library/react";
import DeployStage from "@/components/DeployStage";
import { BrowserRouter } from "react-router-dom";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";

test(`DeployStage component`, async () => {
  const { container, getByTestId } = render(
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <DeployStage
          inComplete={false}
          diffWithStage={false}
          metadataKey="a"
          componentId="b"
        />
      </QueryClientProvider>
    </BrowserRouter>
  );
  expect(container).toBeInTheDocument();
  const btn = getByTestId("deploy-to-stage");
  fireEvent.click(btn);
});
