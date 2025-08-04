import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import NewBuyerModal from "@/pages/Buyer/components/NewBuyerModal";
import TokenModal from "@/pages/Buyer/components/TokenModal";
import {IBuyerToken} from "@/utils/types/component.type";

test("Buyer modal", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <NewBuyerModal open={true} onClose={vi.fn()} currentEnv="abc" />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("Token modal", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <TokenModal
          open={true}
          onClose={vi.fn()}
          item={
            {
              buyerToken: {
                accessToken: "accessToken",
                expiredAt: "",
              },
            } as IBuyerToken
          }
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
