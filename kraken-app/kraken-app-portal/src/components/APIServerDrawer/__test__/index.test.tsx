import * as productHooks from "@/hooks/product";
import { useAppStore } from "@/stores/app.store";
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from "@tanstack/react-query";
import { render, renderHook, screen } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { vi } from "vitest";
import APIServerDrawer from "../index";

test("APIServerDrawer renders correctly when open", () => {
  // Mock hook return values
  const { result } = renderHook(() => useAppStore());
  result.current.setCurrentProduct("testProduct");

  vi.spyOn(productHooks, "useGetComponentListAPI").mockReturnValue({
    data: {
      data: [],
    },
    isLoading: false,
  } as any);

  vi.spyOn(productHooks, "useGetComponentSpecDetails").mockReturnValue({
    data: {
      endpointUsage: {
        controlPlane: [
          {
            facets: {
              endpoints: [{ method: "GET", path: "/test-path" }],
              trigger: { method: "POST", path: "/trigger-path" },
            },
            metadata: {
              id: "1",
              key: "testKey",
              name: "",
              version: 0,
              description: "",
              productKey: "",
              tags: [],
              labels: { test: "t" },
              referApiSpec: "",
              referWorkflow: "",
              logo: "",
            },
            kind: "",
            apiVersion: "",
            links: [],
            id: "",
            parentId: "",
            createdAt: "",
            updatedAt: "",
            syncMetadata: {
              fullPath: "",
              syncedSha: "",
              syncedAt: "",
            },
          },
        ],
        dataPlaneProduction: [],
        dataPlaneStage: [],
      },
    },
  } as any);

  // Render component
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <APIServerDrawer
          isOpen={true}
          onClose={vi.fn()}
          item={
            {
              metadata: {
                key: "testKey",
                id: "",
                name: "",
                version: 0,
                description: "",
                labels: {},
              },
              facets: {},
            } as any
          }
        />
      </BrowserRouter>
    </QueryClientProvider>
  );

  // Check that component is rendered and open
  expect(container).toBeInTheDocument();
  expect(screen.getByText("Check details")).toBeInTheDocument();
  expect(screen.getAllByText(/Endpoints used in/i).length).toEqual(3);
  expect(screen.getByText("/test-path")).toBeInTheDocument();
  expect(screen.getAllByText("No any endpoints used").length).toEqual(2);
});
