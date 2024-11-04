import { users, mappingTemplateRelease } from "@/__mocks__";
import { fireEvent, render, waitFor } from "@/__test__/utils";
import * as mappingHooks from "@/hooks/mappingTemplate";
import * as userHooks from "@/hooks/user";
import MappingTemplateV2 from "@/pages/MappingTemplatev2";
import UpgradePlane from "@/pages/MappingTemplatev2/UpgradePlane";

beforeEach(() => {
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

    const { getByTestId, getAllByTestId, getByText } = render(
      <MappingTemplateV2 />
    );

    await waitFor(() =>
      expect(getByTestId("pageTitle")).toHaveTextContent(
        "Mapping template release & Upgrade v2"
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
    expect(getByTestId("step2")).toHaveTextContent("Stage data plane update");
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

    // Upgrade process
    expect(getByTestId("upgradeProcessTitle")).toHaveTextContent(
      "Upgrade status"
    );
    expect(getByText("No upgrade")).toBeInTheDocument();

    const btnUpgrade = getByTestId("btnCheckUpgrade");
    expect(btnUpgrade).toHaveTextContent("Check and upgrade");

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

    expect(getAllByTestId("upgradedBy")[0]).toHaveTextContent("Miya Chen");
  });

  it("should render data plane upgrade page", async () => {
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

    const { getAllByTestId, getByText } = render(<UpgradePlane />);

    // Breadcrumbs
    const breadcrumbs = getAllByTestId("breadcrumItem");
    expect(breadcrumbs).toHaveLength(2);
    expect(breadcrumbs[0]).toHaveTextContent(
      "Mapping template release & Upgrade v2"
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
  });
});
