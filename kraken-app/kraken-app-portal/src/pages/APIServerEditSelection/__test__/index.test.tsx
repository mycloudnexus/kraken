import { render } from "@/__test__/utils";
import APIServerEditSelection from "..";
import * as productHooks from '@/hooks/product'

describe("API server edit selection", () => {
  it('should render without detail data', () => {
    const { container, getByText } = render(
      <APIServerEditSelection />
    );
    expect(container).toBeInTheDocument();
    expect(getByText('Seller API Setup')).toBeInTheDocument()
    expect(getByText('OK')).toBeInTheDocument()
    expect(getByText('Cancel')).toBeInTheDocument()
  })

  it('should render with detail data', () => {
    vi.spyOn(productHooks, "useGetComponentDetail").mockReturnValue({
      data: {
        facets: {
          baseSpec: {
            content: "content",
          },
          selectedAPIS: [],
          metadata: {
            name: "metadata",
            version: "1.0"
          }
        },
      },
      isLoading: false,
      isFetching: false,
      isFetched: true
    } as any)

    const { container } = render(
      <APIServerEditSelection />
    );
    expect(container).toBeInTheDocument();
  })
});
