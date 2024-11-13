import { users, mappingTemplateRelease } from "@/__mocks__";
import { fireEvent, render, waitFor } from "@/__test__/utils";
import * as mappingHooks from "@/hooks/mappingTemplate";
import * as userHooks from "@/hooks/user";
import MappingTemplateV2 from "@/pages/MappingTemplate";
import UpgradePlane from "@/pages/MappingTemplate/DataPlaneUpgrade";
import ProductionUpgrade from "@/pages/MappingTemplate/DataPlaneUpgrade/ProductionUpgrade";
import StageUpgrade from "@/pages/MappingTemplate/DataPlaneUpgrade/StageUpgrade";

afterEach(() => {
  vi.clearAllMocks();
});

describe("Mapping template v2 component testing", () => {
  it("should render mapping template v2 page", async () => {
    vi.spyOn(mappingHooks, "useInfiniteReleaseHistoryQuery").mockReturnValue({
      data: {
        pages: [
          {
            data: {
              data: mappingTemplateRelease,
              total: 3,
              page: 0,
              size: 10,
            },
          },
        ],
      },
      hasNextPage: false,
      isFetching: false,
      isFetchingNextPage: false,
      fetchNextPage: vi.fn(),
    } as any);

    vi.spyOn(userHooks, "useGetUserList").mockReturnValue({
      data: {
        data: users,
        total: users.length,
        page: 0,
        size: 10000,
      },
      isLoading: false,
      isFetching: false,
      isFetched: true,
    } as any);

    vi.spyOn(userHooks, 'useGetSystemInfo').mockReturnValue({
      data: {
        controlProductVersion: 'V1.1.c',
        stageProductVersion: 'V1.1.s',
        productionProductVersion: 'V1.1.p'
      },
      isFetching: false,
      isLoading: false,
    } as any)

    const { getByTestId, getAllByTestId, getByText } = render(
      <MappingTemplateV2 />
    );

    await waitFor(() =>
      expect(getByTestId("pageTitle")).toHaveTextContent(
        "Mapping template release & Upgrade"
      )
    );

    // Release history
    await waitFor(() => expect(getByTestId("heading")).toBeInTheDocument());
    expect(getByTestId("heading")).toHaveTextContent(
      "What’s new of each release"
    );
    expect(getByTestId("meta")).toHaveTextContent(
      "Process to upgrade Kraken to new version"
    );

    // Guidance steps
    expect(getByTestId("step1")).toHaveTextContent("Control plane upgrade");
    expect(getByTestId("step2")).toHaveTextContent("Stage data plane upgrade");
    expect(getByTestId("step3")).toHaveTextContent("Test offline");
    expect(getByTestId("step4")).toHaveTextContent(
      "Production data plane upgrade"
    );
    expect(getByTestId("step5")).toHaveTextContent("Upgrade done!");

    // List release version
    expect(getByTestId("versionListTitle")).toHaveTextContent("Releases");

    expect(
      getAllByTestId("releaseVersion").map((el) => el.textContent)
    ).toEqual(mappingTemplateRelease.map((rel) => rel.productVersion));
    expect(getAllByTestId("productSpec").map((el) => el.textContent)).toEqual(
      mappingTemplateRelease.map((rel) => rel.productSpec)
    );
    expect(getAllByTestId("releaseStatus").map((el) => el.textContent)).toEqual(
      mappingTemplateRelease.map((rel) => rel.status ?? "Not upgraded")
    );

    // Release details
    await waitFor(() =>
      expect(getByTestId("releaseNoteTitle")).toHaveTextContent("Release note")
    );
    expect(getByTestId("detailVersion")).toHaveTextContent(
      mappingTemplateRelease[0].productVersion
    );
    expect(getByTestId("releaseNote")).toHaveTextContent(
      mappingTemplateRelease[0].description
    );

    // Upgrade versions
    await waitFor(() =>
      expect(getByTestId('controlePlaneUpgradeVersion')).toHaveTextContent('V1.1.c')
    )
    expect(getByTestId('stageUpgradeVersion')).toHaveTextContent('V1.1.s')
    expect(getByTestId('productionUpgradeVersion')).toHaveTextContent('V1.1.p')

    // Upgrade process
    expect(getByTestId("upgradeProcessTitle")).toHaveTextContent(
      "Upgrade status"
    );
    expect(getByText("No upgrade")).toBeInTheDocument();

    const btnUpgrade = getByTestId("btnCheckUpgrade");
    expect(btnUpgrade).toHaveTextContent("Start upgrading");

    // Simuate selecting another release version to view details
    const releaseItems = getAllByTestId("releaseVersionItem");
    expect(releaseItems.length).toBe(mappingTemplateRelease.length);
    fireEvent.click(releaseItems[1]);

    expect(getByTestId("detailVersion")).toHaveTextContent(
      mappingTemplateRelease[1].productVersion
    );
    expect(getByTestId("releaseNote")).toHaveTextContent(
      mappingTemplateRelease[1].description
    );
  });

  it("should render controle plane upgrade step - happy case", async () => {
    vi.spyOn(
      mappingHooks,
      "useGetTemplateMappingReleaseDetail"
    ).mockReturnValue({
      data: {
        templateUpgradeId: "33dea20e-3f0f-45a1-8fc2-5914742e5b8f",
        name: "V1.5.2",
        productVersion: "V1.5.2",
        productSpec: "grace",
        publishDate: "2024-10-28",
        description:
          "Refactor the error tips for quote detail, order detail and inventory list page.\n",
        deployments: [],
        showStageUpgradeButton: false,
        showProductionUpgradeButton: false,
        status: "Upgrading",
      },
      isLoading: false,
      isFetching: false,
      isFetched: true,
      refetch: vi.fn(),
    } as any);

    // left panel
    vi.spyOn(
      mappingHooks,
      "useGetListTemplateUpgradeApiUseCases"
    ).mockReturnValue({
      data: [
        {
          details: [
            {
              targetKey: "mef.sonata.api-target.order.uni.delete",
              targetMapperKey: "mef.sonata.api-target-mapper.order.uni.delete",
              description: "This operation deletes a ProductOrder entity",
              path: "/mefApi/sonata/productOrderingManagement/v10/productOrder",
              method: "post",
              mappingStatus: "incomplete",
              requiredMapping: true,
              diffWithStage: true,
              productType: "uni",
              actionType: "delete",
              mappingMatrix: {
                productType: "uni",
                actionType: "delete",
              },
            },
          ],
        },
      ],
      isLoading: false,
      isFetching: false,
      isFetched: true,
    } as any);

    // right panel
    vi.spyOn(mappingHooks, "useGetListApiUseCases").mockReturnValue({
      data: [
        {
          details: [
            {
              targetKey: "mef.sonata.api-target.order.uni.delete",
              targetMapperKey: "mef.sonata.api-target-mapper.order.uni.delete",
              description: "This operation deletes a ProductOrder entity",
              path: "/mefApi/sonata/productOrderingManagement/v10/productOrder",
              method: "post",
              mappingStatus: "incomplete",
              updatedAt: "2024-10-14T08:22:22.619477Z",
              updatedBy: "8f58d5a9-21e8-42d4-9789-cc747aade876",
              requiredMapping: true,
              diffWithStage: true,
              productType: "uni",
              actionType: "delete",
              mappingMatrix: {
                productType: "uni",
                actionType: "delete",
              },
            },
          ],
        },
      ],
      isLoading: false,
      isFetching: false,
      isFetched: true,
    } as any);

    const handleUpgrade = vi.fn()
    vi.spyOn(mappingHooks, 'useControlPlaneTemplateUpgrade').mockReturnValue({
      mutateAsync: handleUpgrade,
      isPending: false,
    } as any)

    const { getAllByTestId, getByText, getByTestId } = render(<UpgradePlane />);

    // Breadcrumbs
    const breadcrumbs = getAllByTestId("breadcrumItem");
    expect(breadcrumbs).toHaveLength(2);
    expect(breadcrumbs[0]).toHaveTextContent(
      "Mapping template release & Upgrade"
    );
    expect(breadcrumbs[1]).toHaveTextContent("V1.5.2 grace");

    // Steps
    expect(getByText("Control plane upgrade")).toBeInTheDocument();
    expect(getByText("Data plane upgrade: Stage")).toBeInTheDocument();
    expect(getByText("Data plane upgrade: Production")).toBeInTheDocument();

    // Should be at step 1
    let mappingListTitles: HTMLElement[] = [];
    await waitFor(() =>
      expect(
        (mappingListTitles = getAllByTestId("mappingListTitle"))
      ).toHaveLength(2)
    );

    expect(mappingListTitles[0]).toHaveTextContent(
      "New template API mappings (1)"
    );
    expect(mappingListTitles[1]).toHaveTextContent(
      "Control plane API mappings (1)"
    );

    const btnUpgrade = getByTestId('btnUpgrade')
    const btnClose = getByTestId('btnClose')
    expect(btnClose).toHaveTextContent('Close')

    expect(btnUpgrade).toHaveTextContent('Upgrade now')

    fireEvent.click(btnUpgrade)

    await waitFor(() => expect(getByText('Yes, continue')).toBeInTheDocument())

    fireEvent.click(getByText('Yes, continue'))
    expect(handleUpgrade).toHaveBeenCalledTimes(1)
  });

  it("should render data plane - stage upgrade step - unhappy case (mapping deprecated)", async () => {
    vi.spyOn(
      mappingHooks,
      "useGetTemplateMappingReleaseDetail"
    ).mockReturnValue({
      data: {
        templateUpgradeId: "33dea20e-3f0f-45a1-8fc2-5914742e5b8f",
        name: "V1.5.2",
        productVersion: "V1.5.2",
        productSpec: "grace",
        publishDate: "2024-10-28",
        description:
          "Refactor the error tips for quote detail, order detail and inventory list page.\n",
        deployments: [
          {
            "deploymentId": "990c77b9-f8c0-4ef8-a1dd-2932066b9b1a",
            "templateUpgradeId": "5188df9f-c491-4ac1-a1e1-0dacdafb8919",
            "envName": "CONTROL_PLANE",
            "productVersion": "V1.5.1",
            "upgradeBy": "93513df9-313e-419b-969e-ed0c0999af29",
            "status": "SUCCESS",
            "createdAt": "2024-11-05T08:26:26.152908Z",
            "updatedAt": "2024-11-05T08:26:29.037334Z"
          }
        ],
        showStageUpgradeButton: false,
        showProductionUpgradeButton: false,
        status: "Upgrading",
      },
      isLoading: false,
      isFetching: false,
      isFetched: true,
      refetch: vi.fn(),
    } as any);

    // left panel
    vi.spyOn(mappingHooks, "useGetListApiUseCases").mockReturnValue({
      data: [{ details: [] }],
      isLoading: false,
      isFetching: false,
    } as any);

    // right panel
    vi.spyOn(
      mappingHooks,
      "useGetDataPlaneApiUseCases"
    ).mockReturnValue({
      data: [],
      isLoading: false,
      isFetching: false,
    } as any);

    // check stage upgrade
    vi.spyOn(mappingHooks, 'useStageUpgradeCheck').mockReturnValue({
      refetch: () => Promise.resolve({
        data: {
          errorMessages: [],
          compatible: true,
          mapperCompleted: false,
          newerTemplate: true,
        },
        isFetching: false,
      }),
      isFetching: false
    } as any)

    const { getAllByTestId, getByText, getByTestId } = render(<UpgradePlane />);

    // Should be at step 2
    let mappingListTitles: HTMLElement[] = [];
    await waitFor(() =>
      expect(
        (mappingListTitles = getAllByTestId("mappingListTitle"))
      ).toHaveLength(2)
    );

    expect(mappingListTitles[0]).toHaveTextContent(
      "Control plane API mappings (0)"
    );
    expect(mappingListTitles[1]).toHaveTextContent(
      "Data plane: Stage API mappings (0)"
    );

    const btnUpgrade = getByTestId('btnUpgrade')
    expect(btnUpgrade).toHaveTextContent('Upgrade now')

    fireEvent.click(btnUpgrade)
    // Should show upgrade confirm modal
    await waitFor(() => expect(getByText('Yes, continue')).toBeInTheDocument())

    fireEvent.click(getByText('Yes, continue'))

    // // Should show deprecated modal
    // await waitFor(() => expect(getByText('A newer version mapping template started to upgarde.')).toBeInTheDocument())
    // const btnOk = getAllByText('Got it')[0]
    // expect(btnOk).toBeInTheDocument()

    // // // Click on Got it, should go back to mapping landing page
    // fireEvent.click(btnOk)
    // await waitFor(() => expect(getByText("What's new of each release")).toBeInTheDocument())
  });

  it("should render data plane - stage upgrade step - happy case", async () => {
    vi.spyOn(
      mappingHooks,
      "useGetTemplateMappingReleaseDetail"
    ).mockReturnValue({
      data: {
        templateUpgradeId: "33dea20e-3f0f-45a1-8fc2-5914742e5b8f",
        name: "V1.5.2",
        productVersion: "V1.5.2",
        productSpec: "grace",
        publishDate: "2024-10-28",
        description:
          "Refactor the error tips for quote detail, order detail and inventory list page.\n",
        deployments: [
          {
            "deploymentId": "990c77b9-f8c0-4ef8-a1dd-2932066b9b1a",
            "templateUpgradeId": "5188df9f-c491-4ac1-a1e1-0dacdafb8919",
            "envName": "CONTROL_PLANE",
            "productVersion": "V1.5.1",
            "upgradeBy": "93513df9-313e-419b-969e-ed0c0999af29",
            "status": "SUCCESS",
            "createdAt": "2024-11-05T08:26:26.152908Z",
            "updatedAt": "2024-11-05T08:26:29.037334Z"
          }
        ],
        showStageUpgradeButton: false,
        showProductionUpgradeButton: false,
        status: "Upgrading",
      },
      isLoading: false,
      isFetching: false,
      isFetched: true,
      refetch: vi.fn(),
    } as any);

    // left panel
    vi.spyOn(mappingHooks, "useGetListApiUseCases").mockReturnValue({
      data: [{
        details: [{
          "targetKey": "mef.sonata.api-target.order.uni.delete",
          "targetMapperKey": "mef.sonata.api-target-mapper.order.uni.delete",
          "description": "This operation deletes a ProductOrder entity",
          "path": "/mefApi/sonata/productOrderingManagement/v10/productOrder",
          "method": "post",
          "mappingStatus": "incomplete",
          "updatedAt": "2024-10-14T08:22:22.619477Z",
          "updatedBy": "8f58d5a9-21e8-42d4-9789-cc747aade876",
          "requiredMapping": true,
          "diffWithStage": true,
          "productType": "uni",
          "actionType": "delete",
          "mappingMatrix": {
            "productType": "uni",
            "actionType": "delete"
          }
        }]
      }],
      isLoading: false,
      isFetching: false,
    } as any);

    // right panel
    vi.spyOn(
      mappingHooks,
      "useGetDataPlaneApiUseCases"
    ).mockReturnValue({
      data: [{
        "targetMapperKey": "mef.sonata.api-target-mapper.order.uni.delete",
        "path": "/mefApi/sonata/productInventory/v7/product/{id}",
        "method": "get",
        "requiredMapping": true,
        "diffWithStage": true,
        "productType": "uni",
        "mappingMatrix": {
          "productType": "uni"
        },
        "componentName": "Product Inventory Management",
        "componentKey": "mef.sonata.api.inventory",
        "createAt": "2024-10-23T07:46:13.806869Z",
        "createBy": "8f58d5a9-21e8-42d4-9789-cc747aade876",
        "userName": "user_upgrade",
        "version": "1.8",
        "status": "SUCCESS",
        "mappingStatus": ""
      }],
      isLoading: false,
      isFetching: false,
    } as any);

    // check stage upgrade
    vi.spyOn(mappingHooks, 'useStageUpgradeCheck').mockReturnValue({
      refetch: () => Promise.resolve({
        data: {
          errorMessages: [],
          compatible: false,
          mapperCompleted: false,
          newerTemplate: false,
        },
        isFetching: false,
      }),
      isFetching: false
    } as any)

    const { getAllByTestId, getByText, getByTestId } = render(<UpgradePlane />);

    // Should be at step 2
    let mappingListTitles: HTMLElement[] = [];
    await waitFor(() =>
      expect(
        (mappingListTitles = getAllByTestId("mappingListTitle"))
      ).toHaveLength(2)
    );

    // Should showing upgrade incomplete alert
    expect(getByText('Please adjust and complete the incomplete mapping use cases that will be upgraded to data plane.')).toBeInTheDocument()

    expect(mappingListTitles[0]).toHaveTextContent(
      "Control plane API mappings (1)"
    );
    expect(mappingListTitles[1]).toHaveTextContent(
      "Data plane: Stage API mappings (1)"
    );

    const btnUpgrade = getByTestId('btnUpgrade')
    expect(btnUpgrade).toHaveTextContent('Upgrade now')
    expect(btnUpgrade).toBeDisabled()
  });

  it("should render data plane - production upgrade step - unhappy case (version incompatible)", async () => {
    vi.spyOn(
      mappingHooks,
      "useGetTemplateMappingReleaseDetail"
    ).mockReturnValue({
      data: {
        templateUpgradeId: "33dea20e-3f0f-45a1-8fc2-5914742e5b8f",
        name: "V1.5.2",
        productVersion: "V1.5.2",
        productSpec: "grace",
        publishDate: "2024-10-28",
        description:
          "Refactor the error tips for quote detail, order detail and inventory list page.\n",
        deployments: [
          {
            "deploymentId": "990c77b9-f8c0-4ef8-a1dd-2932066b9b1a",
            "templateUpgradeId": "5188df9f-c491-4ac1-a1e1-0dacdafb8919",
            "envName": "CONTROL_PLANE",
            "productVersion": "V1.5.1",
            "upgradeBy": "93513df9-313e-419b-969e-ed0c0999af29",
            "status": "SUCCESS",
            "createdAt": "2024-11-05T08:26:26.152908Z",
            "updatedAt": "2024-11-05T08:26:29.037334Z",
          },
          {
            "deploymentId": "990c77b9-f8c0-4ef8-a1dd-2932066b9b1a",
            "templateUpgradeId": "5188df9f-c491-4ac1-a1e1-0dacdafb8919",
            "envName": "stage",
            "productVersion": "V1.5.1",
            "upgradeBy": "93513df9-313e-419b-969e-ed0c0999af29",
            "status": "SUCCESS",
            "createdAt": "2024-11-05T08:26:26.152908Z",
            "updatedAt": "2024-11-05T08:26:29.037334Z",
          }
        ],
        showStageUpgradeButton: false,
        showProductionUpgradeButton: false,
        status: "Upgrading",
      },
      isLoading: false,
      isFetching: false,
      refetch: vi.fn(),
    } as any);

    // left panel
    vi.spyOn(mappingHooks, "useGetDataPlaneApiUseCases").mockReturnValue({
      data: [],
      isLoading: false,
      isFetching: false,
    } as any);

    // right panel
    vi.spyOn(
      mappingHooks,
      "useGetDataPlaneApiUseCases"
    ).mockReturnValue({
      data: [],
      isLoading: false,
      isFetching: false,
    } as any);

    // check stage upgrade
    vi.spyOn(mappingHooks, 'useProductionUpgradeCheck').mockReturnValue({
      refetch: () => Promise.resolve({
        data: {
          errorMessages: [],
          compatible: false,
          mapperCompleted: false,
          newerTemplate: false
        }
      }),
      isFetching: false
    } as any)

    const { getByText, getByTestId } = render(<UpgradePlane />);

    const btnUpgrade = getByTestId('btnUpgrade')
    expect(btnUpgrade).toHaveTextContent('Upgrade now')

    fireEvent.click(btnUpgrade)
    // Should show upgrade confirm modal
    await waitFor(() => expect(getByText('Yes, continue')).toBeInTheDocument())

    fireEvent.click(getByText('Yes, continue'))

    await waitFor(() => expect(getByText('Mapping template upgrade successfully in production data plane but not compatible with Kraken version running in this plane. Please upgrade kraken to make this new mapping template effective.')).toBeInTheDocument())
  });

  it("should render data plane - production upgrade step - happy case", async () => {
    vi.spyOn(
      mappingHooks,
      "useGetTemplateMappingReleaseDetail"
    ).mockReturnValue({
      data: {
        templateUpgradeId: "33dea20e-3f0f-45a1-8fc2-5914742e5b8f",
        name: "V1.5.2",
        productVersion: "V1.5.2",
        productSpec: "grace",
        publishDate: "2024-10-28",
        description:
          "Refactor the error tips for quote detail, order detail and inventory list page.\n",
        deployments: [
          {
            "deploymentId": "990c77b9-f8c0-4ef8-a1dd-2932066b9b1a",
            "templateUpgradeId": "5188df9f-c491-4ac1-a1e1-0dacdafb8919",
            "envName": "CONTROL_PLANE",
            "productVersion": "V1.5.1",
            "upgradeBy": "93513df9-313e-419b-969e-ed0c0999af29",
            "status": "SUCCESS",
            "createdAt": "2024-11-05T08:26:26.152908Z",
            "updatedAt": "2024-11-05T08:26:29.037334Z",
          },

          {
            "deploymentId": "990c77b9-f8c0-4ef8-a1dd-2932066b9b1a",
            "templateUpgradeId": "5188df9f-c491-4ac1-a1e1-0dacdafb8919",
            "envName": "stage",
            "productVersion": "V1.5.1",
            "upgradeBy": "93513df9-313e-419b-969e-ed0c0999af29",
            "status": "SUCCESS",
            "createdAt": "2024-11-05T08:26:26.152908Z",
            "updatedAt": "2024-11-05T08:26:29.037334Z",
          }
        ],
        showStageUpgradeButton: false,
        showProductionUpgradeButton: false,
        status: "Upgrading",
      },
      isLoading: false,
      isFetching: false,
      isFetched: true,
      refetch: vi.fn(),
    } as any);

    // left panel
    vi.spyOn(mappingHooks, "useGetDataPlaneApiUseCases").mockReturnValue({
      data: [],
      isLoading: false,
      isFetching: false,
    } as any);

    // right panel
    vi.spyOn(
      mappingHooks,
      "useGetDataPlaneApiUseCases"
    ).mockReturnValue({
      data: [],
      isLoading: false,
      isFetching: false,
    } as any);

    // check stage upgrade
    const checkProductionUpgrade = vi.fn()
    vi.spyOn(mappingHooks, 'useProductionUpgradeCheck').mockReturnValue({
      data: {
        errorMessages: [],
        compatible: true,
        mapperCompleted: false,
        newerTemplate: false
      },
      refetch: checkProductionUpgrade,
      isFetching: false
    } as any)

    const { getAllByTestId, getByText, getByTestId } = render(<UpgradePlane />);

    // Should be at step 2
    let mappingListTitles: HTMLElement[] = [];
    await waitFor(() =>
      expect(
        (mappingListTitles = getAllByTestId("mappingListTitle"))
      ).toHaveLength(2)
    );

    expect(mappingListTitles[0]).toHaveTextContent(
      "Data plane (stage) API mappings (0)"
    );
    expect(mappingListTitles[1]).toHaveTextContent(
      "Data plane (production) API mappings (0)"
    );

    const btnUpgrade = getByTestId('btnUpgrade')
    expect(btnUpgrade).toHaveTextContent('Upgrade now')

    fireEvent.click(btnUpgrade)
    // Should show upgrade confirm modal
    await waitFor(() => expect(getByText('Yes, continue')).toBeInTheDocument())

    fireEvent.click(getByText('Yes, continue'))
    expect(checkProductionUpgrade).toHaveBeenCalledTimes(1)
  });

  it('should render stage upgrade component', () => {
    const { container } = render(<StageUpgrade />)
    expect(container).toBeInTheDocument()
  })

  it('should render production upgrade component', () => {
    const { container } = render(<ProductionUpgrade />)
    expect(container).toBeInTheDocument()
  })
});
