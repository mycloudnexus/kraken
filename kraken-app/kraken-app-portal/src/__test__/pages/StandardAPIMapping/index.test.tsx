import * as productHooks from "@/hooks/product";
import StandardAPIMapping from "@/pages/StandardAPIMapping";
import * as mappingStore from "@/stores/mappingUi.store";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

test("StandardAPIMapping btn-create-version", () => {
  vi.spyOn(mappingStore, "useMappingUiStore").mockReturnValue({
    activePath: "/a/b/c/d/e",
    setActivePath: vi.fn(),
    selectedKey: "targetKey",
    setSelectedKey: vi.fn(),
  });

  vi.spyOn(productHooks, "useGetComponentDetailMapping").mockReturnValue({
    data: {
      details: [
        {
          description: "mock_mapping",
          mappingMatrix: {},
          mappingStatus: "in progress",
          method: "GET",
          orderBy: "createdAt",
          path: "/a/b/c/d/e",
          requiredMapping: false,
          targetKey: "targetKey",
          targetMapperKey: "targetMapperKey",
          updatedAt: "2024-12-3T01:22:00Z",
          actionType: "actionType",
          diffWithStage: false,
          lastDeployedAt: "2024-12-3T01:22:00Z",
          order: 1,
          productType: "productType",
        },
      ],
    },
    isLoading: false,
    isFetching: false,
    isFetched: true,
  } as any);

  const { container, getByTestId } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <StandardAPIMapping />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();

  // Simulate resizing left panel size
  const leftPanel = getByTestId("leftPanel");

  fireEvent.mouseMove(leftPanel, {
    clientX: 400,
    clientY: 400,
  });
  fireEvent.mouseUp(leftPanel);
});
