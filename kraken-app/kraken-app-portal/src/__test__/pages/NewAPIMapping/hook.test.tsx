import { useCommonAddProp } from "@/pages/NewAPIMapping/components/commonHook";
import { renderHook } from "@testing-library/react";

describe("test hook", () => {
  test("hook have value", () => {
    const mockFn = vi.fn();
    const { result } = renderHook(() =>
      useCommonAddProp({
        pathParameters: [
          { name: "a", schema: { type: "object" } },
          { name: "a1", schema: { type: "string" } },
        ],
        onSelect: mockFn,
        setSelectedProp: mockFn,
        selectedProp: { location: "PATH", name: "a" },
        rightSideInfo: {
          method: "update",
          previousData: {
            name: "mapper.order.uni.delete.buyerId",
            title:
              "The unique identifier of the organization that is acting as the a Buyer.",
            source: "@{{query.buyerId}}",
            target: "",
            description: "",
            sourceLocation: "QUERY",
            targetLocation: "",
            requiredMapping: false,
          },
          title:
            "The unique identifier of the organization that is acting as the a Buyer.",
        },
        queryParameters: [
          { name: "b", schema: { type: "object" } },
          { name: "b1", schema: { type: "string" } },
        ],
        requestBodyTree: undefined,
      })
    );
    expect(result.current.handleAddProp).toBeDefined();
    expect(result.current.collapseItems).toBeDefined();
  });
});
