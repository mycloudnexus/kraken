import { render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import AuditLogModal from "@/pages/AuditLog/components/AuditLogDetailsModal";

test("Buyer modal", () => {
  const item = 
  {
      id: "9410831d-4669-bcc2-258349753440",
      createdBy: "dbae-4ad3-a895-fe58e4a0120a",
      createdAt: "2024-08-19T03:07:41.120153Z",
      updatedAt: "2024-08-19T03:07:41.368493Z",
      userId: "dbae-4ad3-a895-fe58e4a0120a",
      email: "john@example.com",
      name: "John Deer",
      path: "/products/mef.sonata/components/4e51-b34b-1119649642d2/targetMapper",
      method: "PATCH",
      pathVariables: {
          productId: "mef.sonata",
          id: "4e51-b34b-1119649642d2"
      },
      action: "UPDATE",
      description: "update target api mapper",
      resource: "api mapping use case",
      resourceId: "4e51-b34b-1119649642d2",
      remoteAddress: "207.226.139.140",
      statusCode: 200,
      ignoreRequestParams: [],
      request: {},
      response: {}
  }
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuditLogModal open={true} onClose={vi.fn()} item={item} />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});
