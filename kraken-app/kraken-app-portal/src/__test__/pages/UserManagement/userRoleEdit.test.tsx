import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import { ERole } from "@/components/Role";
import dayjs from "dayjs";
import UserRoleEdit from "@/components/AuthProviders/basic/components/UserManagement/components/UserRoleEdit";

test("UserModal test", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <UserRoleEdit
          isAdmin
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
        <UserRoleEdit
          isAdmin
          user={{
            role: ERole.USER,
            name: "a",
            email: "b",
            id: "c",
            createdAt: dayjs().format(),
            createdBy: "e",
            productId: "d",
            state: "DISABLED",
            updatedAt: dayjs().format(),
            updatedBy: "f",
          }}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
