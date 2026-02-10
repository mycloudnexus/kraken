import { render } from "@/__test__/utils";
import * as hooks from "@/hooks/product";
import DeployHistory from "@/pages/NewAPIMapping/components/DeployHistory";
import { vi } from "vitest";

vi.mock("antd", async () => {
  const actual = await vi.importActual<any>("antd");
  return {
    ...actual,
    Table: ({ columns, dataSource, locale }: any) => {
      return (
        <div data-testid="mock-table">
          {dataSource?.length === 0 && locale?.emptyText}

          <div className="mock-headers">
            {columns.map((col: any, index: number) => (
              <div key={col.key || col.dataIndex || index}>
                {typeof col.title === "string" ? col.title : "Complex Title"}
              </div>
            ))}
          </div>

          {dataSource?.map((record: any, rowIndex: number) => (
            <div key={record.id || rowIndex} className="mock-row">
              {columns.map((col: any, colIndex: number) => {
                const cellValue = col.dataIndex ? record[col.dataIndex] : record;
                const content = col.render
                  ? col.render(cellValue, record, rowIndex)
                  : cellValue;

                const cellKey = col.key || col.dataIndex || colIndex;
                return <div key={cellKey}>{content}</div>;
              })}
            </div>
          ))}
        </div>
      );
    },
    Tooltip: ({ children }: any) => <div>{children}</div>,
    Switch: () => <input type="checkbox" />,
    Result: ({ subTitle }: any) => <div>{subTitle}</div>,
  };
});

beforeEach(() => {
  vi.clearAllMocks();
});

describe("Deployment > Deployment history tests", () => {
  it("should render blank deployment history table", () => {
    vi.spyOn(hooks, "useGetAPIDeployments").mockImplementation(
      vi.fn().mockReturnValue({
        data: {
          data: [],
          page: 0,
          size: 20,
          total: 0,
        },
        isFetching: false,
        isLoading: false,
        isFetched: true,
      })
    );

    const { getByText } = render(<DeployHistory scrollHeight={1000} />);

    expect(getByText("Version")).toBeInTheDocument();
    expect(getByText("Environment")).toBeInTheDocument();
    expect(getByText("Deployed by")).toBeInTheDocument();
    expect(getByText("Verified by")).toBeInTheDocument();
    expect(getByText("Actions")).toBeInTheDocument();

    expect(getByText("No deploy history")).toBeInTheDocument();
  });

  it('should render deploy history', () => {
    vi.spyOn(hooks, 'useGetAPIDeployments').mockReturnValue({
      data: {
        data: [
          {
            "targetMapperKey": "mef.sonata.api-target-mapper.order.eline.read.delete",
            "path": "/mefApi/sonata/productOrderingManagement/v10/productOrder/{id}",
            "method": "get",
            "requiredMapping": true,
            "diffWithStage": true,
            "productType": "access_e_line",
            "actionType": "delete",
            "mappingMatrix": {
              "productType": "access_e_line",
              "actionType": "delete"
            },
            "componentName": "Product Ordering Management",
            "componentKey": "mef.sonata.api.order",
            "envName": "production",
            "createAt": "2024-11-18T10:06:14.412978Z",
            "createBy": "24044233-3683-425e-9cbe-62e643f081d8",
            "userName": "user",
            "releaseKey": "mef.sonata.product-release.1731924374407",
            "version": "1.4",
            "status": "SUCCESS",
            "verifiedBy": "24044233-3683-425e-9cbe-62e643f081d8",
            "verifiedAt": "2024-11-18 10:06:11",
            "verifiedStatus": true,
            "productionEnable": false
          }
        ],
        total: 1,
        page: 0,
        size: 10,
      },
      isLoading: false,
      isFetching: false,
      isFetched: true,
    } as any)

    const { getByText } = render(<DeployHistory scrollHeight={1000} selectedEnv={{ id: 'abc', name: 'stage' } as any} />)
    expect(getByText('API mapping')).toBeInTheDocument()
  })
});