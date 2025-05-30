import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import { ERole } from "@/components/Role";
import dayjs from "dayjs";
import ResetPwdModal from "@/components/AuthProviders/basic/components/UserManagement/components/ResetPwdModal";
import ResetPwd from "@/components/AuthProviders/basic/components/UserManagement/components/ResetPwd";

test("ResetPwdModal test", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ResetPwdModal
          onClose={vi.fn()}
          open
          user={{
            role: ERole.ADMIN,
            name: "a",
            email: "b",
            id: "c",
            createdAt: dayjs().format(),
            createdBy: "e",
            productId: "d",
            state: "ENABLED",
            updatedAt: dayjs().format(),
            updatedBy: "f",
          }}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});

test("ResetPwd test", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ResetPwd
          user={{
            role: ERole.ADMIN,
            name: "a",
            email: "b",
            id: "c",
            createdAt: dayjs().format(),
            createdBy: "e",
            productId: "d",
            state: "ENABLED",
            updatedAt: dayjs().format(),
            updatedBy: "f",
          }}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
