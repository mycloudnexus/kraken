import { render } from "@/__test__/utils";
import * as hooks from "@/hooks/product";
import DeployHistory from "@/pages/NewAPIMapping/components/DeployHistory";

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

    const { getByText } = render(<DeployHistory />);

    // Table headings assertions
    expect(getByText("Version")).toBeInTheDocument();
    expect(getByText("Environment")).toBeInTheDocument();
    expect(getByText("Deployed by")).toBeInTheDocument();
    expect(getByText("Verified for Production")).toBeInTheDocument();
    expect(getByText("Verified by")).toBeInTheDocument();
    expect(getByText("Actions")).toBeInTheDocument();

    expect(getByText("No deploy history")).toBeInTheDocument();
  });
});
