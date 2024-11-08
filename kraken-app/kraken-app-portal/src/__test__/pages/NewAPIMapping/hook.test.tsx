import { useCommonAddProp } from "@/pages/NewAPIMapping/components/commonHook";
import { locationMapping, renderDeployText, validateMappers } from "@/pages/NewAPIMapping/helper";
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

describe('api mapping helper functions', () => {
  it('should return corresponding location mapping text', () => {
    const params: Array<Array<string>> = [['BODY', 'request'], ['BODY', 'response'], ['QUERY'], ['PATH'], ['HYBRID'], ['CONSTANT']]
    const output: Array<string> = ['Request body', 'Response body', 'Query parameter', 'Path parameter', 'Hybrid', 'Constant']
    
    for (let i = 0; i < params.length; i++) {
      expect(
        // eslint-disable-next-line prefer-spread
        locationMapping.apply(null, params[i] as any)
      ).toBe(output[i])
    }
  })

  it('should return corresponding deploy text', () => {
    const params: Array<Array<string>> = [['SUCCESS'], ['IN_PROCESS'], ['FAILED']]
    const output: Array<string> = ['success.', 'in process.', 'failed.']
    
    for (let i = 0; i < params.length; i++) {
      expect(
        // eslint-disable-next-line prefer-spread
        renderDeployText.apply(null, params[i] as any)
      ).toBe(output[i])
    }
  })

  it('should validate api mapping 1', () => {
    const { requestIds, responseIds, errorMessage } = validateMappers({
      request: [
        {
          id: '1',
          customizedField: false,
          target: 'target1',
          targetLocation: 'targetLocation1'
        },
        {
          id: '2',
          customizedField: true,
          source: 'source2',
          sourceLocation: 'sourceLocation2',
        }
      ],
      response: []
    } as any)

    expect(requestIds).toEqual(new Set(['2']))
    expect(responseIds).toEqual(new Set())
    expect(errorMessage).toBe("Some customized properties from Request mapping are empty, please check.")
  })

  it('should validate api mapping 2', () => {
    const { requestIds, responseIds, errorMessage } = validateMappers({
      request: [],
      response: [
        {
          id: '3',
          customizedField: false,
          source: 'source1',
        },
        {
          id: '4',
          customizedField: true,
          target: 'target2',
          targetLocation: 'targetLocation2'
        }
      ]
    } as any)

    expect(requestIds).toEqual(new Set())
    expect(responseIds).toEqual(new Set(['3', '4']))
    expect(errorMessage).toBe("Some customized properties from Response mapping are empty, please check.")
  })

  it('should validate api mapping 1', () => {
    const { requestIds, responseIds, errorMessage } = validateMappers({
      request: [
        {
          id: '1',
          customizedField: false,
          target: 'target1',
        },
        {
          id: '2',
          customizedField: true,
          source: 'source2',
          target: 'target2',
          targetLocation: 'targetLocation2'
        }
      ],
      response: [
        {
          id: '3',
          customizedField: false,
          source: 'source1',
        },
        {
          id: '4',
          customizedField: true,
          source: 'source2',
          sourceLocation: 'sourceLocation2',
          target: 'target2',
        }
      ]
    } as any)

    expect(requestIds).toEqual(new Set(['1', '2']))
    expect(responseIds).toEqual(new Set(['3', '4']))
    expect(errorMessage).toBe("Some customized properties from Request and Response mapping are empty, please check.")
  })
})
