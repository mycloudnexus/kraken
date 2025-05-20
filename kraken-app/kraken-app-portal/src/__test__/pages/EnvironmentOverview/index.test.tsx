import EnvironmentOverview from "@/pages/EnvironmentOverview";
import * as productHooks from "@/hooks/product";
import * as sizeHooks from '@/hooks/useContainerHeight'
import { fireEvent, render, waitFor } from "@/__test__/utils";

import {screen} from "@/__test__/utils.tsx";
const ResizeObserverMock = vi.fn(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

// Stub the global ResizeObserver
vi.stubGlobal('ResizeObserver', ResizeObserverMock);

describe(" Environment Overview component list", () => {
  beforeAll(() => {
    vi.spyOn(productHooks, "useGetProductEnvs").mockReturnValue({
      data: {
        data: [
          {
            id: "32b4832f-fb2f-4c99-b89a-c5c995b18dfc",
            productId: "mef.sonata",
            createdAt: "2024-05-30T13:02:03.224486Z",
            name: "production",
          },
        ],
      },
      isLoading: false,
    } as any);
    vi.spyOn(productHooks, "useGetRunningAPIList").mockReturnValue({
      data: [
        {
          id: "b2d775e5-44ad-43cb-8dd4-6fbe52585ec9",
          createdAt: "2024-08-01 01:55:27",
          updatedAt: "2024-08-01 01:55:27",
          name: "production",
          components: [
            {
              version: "1.0",
              key: "mef.sonata.api.order",
              componentName: "Product Ordering Management",
            },
            {
              version: "1.1",
              key: "mef.sonata.api.inventory",
              componentName: "Product Inventory Management",
            },
            {
              version: "1.0",
              key: "mef.sonata.api.serviceability.address",
              componentName: "Geographic Address Management",
            },
            {
              version: "1.0",
              key: "mef.sonata.api.quote",
              componentName: "Quote Management",
            },
          ],
        },
      ],
      isLoading: false,
    } as any);
  });

  it("running components list", async () => {
    vi.clearAllMocks()
    vi.spyOn(sizeHooks, 'useContainerHeight').mockReturnValue([1000])

    const { getByText, getAllByRole } = render(
      <EnvironmentOverview />
    );
    const ele = getByText("production Environment");
    expect(ele).toBeInTheDocument();

    const tabs = getAllByRole('radio')

    expect(tabs).toHaveLength(2)
    expect(tabs[0].parentNode?.parentNode).toHaveTextContent('Running API mappings')
    expect(tabs[1].parentNode?.parentNode).toHaveTextContent('Deployment history')

    // Should default open running api mapping tab
    await waitFor(() => expect(getByText('Component')).toBeInTheDocument())

    // Open deployment history tab
    fireEvent.click(tabs[1])
    await waitFor(() => expect(getByText('API mapping')).toBeInTheDocument())
  });

  it("rotateApiKeyMutate", async () => {
    vi.spyOn(sizeHooks, 'useContainerHeight').mockReturnValue([1000])

    vi.spyOn(productHooks, "useGetAllApiKeyList").mockReturnValue({
        data: {
          data:[
              {
                "id": "a4d4097a-0188-4fab-9f52-2d8352210a5d",
                "productId": "mef.sonata",
                "envId": "32b4832f-fb2f-4c99-b89a-c5c995b18dfc"
              }
           ]
       },
      isLoading: false,
    } as any);
    const useRotateApiKeySpy= vi.spyOn(productHooks, "useRotateApiKey").mockReturnValue({
      data: {
        data:[
          {
            "token": "XXXX"
          }
        ]
      },
      isLoading: false,
    } as any);
    const { getByText, getByRole,findByRole } = render(
        <EnvironmentOverview />
    );

    const envElement = getByText("production Environment");
    fireEvent.click(envElement);

    const dropdownMenu = getByRole("img", {
      name: 'more'
    });
    fireEvent.mouseEnter(dropdownMenu);

    await waitFor(() => {
      expect(screen.getByText('Rotate API Key')).toBeInTheDocument();
    });
    const rotateMenuItem = await findByRole("menuitem", {
      name: /Rotate API Key/i,
    });
    fireEvent.click(rotateMenuItem);

    const rotateButton = await findByRole("button", {
      name: /Rotate/i,
    });
    fireEvent.click(rotateButton);
    expect(useRotateApiKeySpy).toBeCalled();
  });
});
