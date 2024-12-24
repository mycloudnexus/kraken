import { fireEvent, render, waitFor } from "@/__test__/utils";
import { ApiCard } from "@/components/ApiMapping";
import { ApiList } from "@/pages/MappingTemplate/DataPlaneUpgrade/components/ApiList";
import { ApiListSkeleton } from "@/pages/MappingTemplate/DataPlaneUpgrade/components/ApiListSkeleton";
import { DeprecatedModal } from "@/pages/MappingTemplate/DataPlaneUpgrade/components/DeprecatedModal";
import { IncompatibleMappingModal } from "@/pages/MappingTemplate/DataPlaneUpgrade/components/IncompatibleMappingModal";
import { StartUpgradeModal } from "@/pages/MappingTemplate/DataPlaneUpgrade/components/StartUpgradeModal";
import { DetailDrawer } from "@/pages/MappingTemplate/components/VersionSelect/DetailDrawer";
import { ListVersionSkeleton } from "@/pages/MappingTemplate/components/VersionSelect/ListVersionSkeleton";
import * as productHooks from '@/hooks/product'
import * as userHooks from '@/hooks/user/useUser'
import { DeploymentStatus } from "@/pages/NewAPIMapping/components/Deployment/DeploymentStatus";
import { VersionSelect } from "@/pages/MappingTemplate/components/VersionSelect";

afterEach(() => {
  vi.clearAllMocks()
})

describe("components used in mapping template v2 pages", () => {
  it("should render deprecated warning modal", () => {
    const handleCancel = vi.fn();
    const handleOk = vi.fn();

    const { getByTestId, getAllByRole } = render(
      <DeprecatedModal open onCancel={handleCancel} onOk={handleOk} />
    );
    expect(getByTestId("title")).toHaveTextContent(
      "This mapping template is depreacted"
    );
    expect(getByTestId("meta")).toHaveTextContent(
      "A newer version mapping template started to upgarde."
    );

    const buttons = getAllByRole("button");
    expect(buttons.length).toBe(2);

    // OK button
    expect(buttons[1]).toHaveTextContent("Got it");
    fireEvent.click(buttons[1]);
    expect(handleOk).toHaveBeenCalledTimes(1);
  });

  it("should render stage version incompatible warning modal", () => {
    const handleCancel = vi.fn();
    const handleOk = vi.fn();

    const { getByTestId, getAllByRole } = render(
      <IncompatibleMappingModal open onCancel={handleCancel} onOk={handleOk} />
    );
    expect(getByTestId("title")).toHaveTextContent(
      "Kraken version running in stage data plane is not compatible with this mapping template"
    );
    expect(getByTestId("meta")).toHaveTextContent(
      "Please ensure to upgrade kraken in stage data plane to a compatible version first and test all the running use cases before moving to production data plane upgrade."
    );

    const buttons = getAllByRole("button");
    expect(buttons.length).toBe(2);

    // OK button
    expect(buttons[1]).toHaveTextContent("Got it");
    fireEvent.click(buttons[1]);
    expect(handleOk).toHaveBeenCalledTimes(1);
  });

  it("should render upgrade confirm modal", () => {
    const handleCancel = vi.fn();
    const handleOk = vi.fn();

    const { getByTestId, getAllByRole } = render(
      <StartUpgradeModal open onCancel={handleCancel} onOk={handleOk} />
    );
    expect(getByTestId("title")).toHaveTextContent(
      "Are you sure to start upgrade now?"
    );
    expect(getByTestId("meta")).toHaveTextContent(
      "Upgrade may take a while. You will not be able to change the API mapping configurations or perform new deployment during the process. Continue?"
    );

    const buttons = getAllByRole("button");
    expect(buttons.length).toBe(3);

    // Cancel button
    expect(buttons[1]).toHaveTextContent("Cancel");

    // OK button
    expect(buttons[2]).toHaveTextContent("Yes, continue");
    fireEvent.click(buttons[2]);
    expect(handleOk).toHaveBeenCalledTimes(1);

    fireEvent.click(buttons[1]);
    expect(handleCancel).toHaveBeenCalledTimes(1);
  });

  it("should render blank upgrade detail drawer", () => {
    vi.spyOn(productHooks, 'useGetMappingTemplateUpgradeDetail').mockReturnValue({
      data: [],
      isLoading: false,
      isFetching: false,
    } as any)

    const { getByText, getByTestId } = render(<DetailDrawer open deploymentId="" />);

    expect(getByTestId("notification")).toHaveTextContent(
      "Following mapping use cases upgraded because of this template upgrade."
    );

    // Table headings
    expect(getByText("Mapping use case")).toBeInTheDocument();
    expect(getByText("Upgrade to")).toBeInTheDocument();
    expect(getByText("Upgrade status")).toBeInTheDocument();

    expect(getByText('No data')).toBeInTheDocument()
  });

  it("should render upgrade detail drawer with upgrade history", () => {
    vi.spyOn(productHooks, 'useGetMappingTemplateUpgradeDetail').mockReturnValue({
      data: [
        {
          "tagId": "c1c3b614-e325-4f94-987d-36e5ad02210e",
          "version": "2.1",
          "mapperKey": "mef.sonata.api-target-mapper.address.validate",
          "componentKey": "mef.sonata.api.serviceability.address",
          "mappingMatrix": {
            "provideAlternative": false,
            "addressType": "FieldedAddress"
          },
          "status": "SUCCESS",
          "method": "post",
          "path": "/a/b/c/d/e"
        }
      ],
      isLoading: false,
      isFetching: false,
    } as any)

    const { getAllByTestId } = render(<DetailDrawer open deploymentId="" />);

    expect(getAllByTestId('apiPath')[0]).toHaveTextContent('/d/e')
    expect(getAllByTestId('mappingType')[0]).toHaveTextContent('provide Alternative')
    expect(getAllByTestId('mappingValue')[0]).toHaveTextContent('FALSE')
    expect(getAllByTestId('mappingType')[1]).toHaveTextContent('address Type')
    expect(getAllByTestId('mappingValue')[1]).toHaveTextContent('FIELDEDADDRESS')
    expect(getAllByTestId('upgradeToVersion')[0]).toHaveTextContent('2.1')
    expect(getAllByTestId('deploymentStatus')[0]).toHaveTextContent('Success')
  });

  it("should render api mapping card", () => {
    const handleClick = vi.fn();

    const { getByTestId, getAllByTestId } = render(
      <ApiCard
        apiInstance={
          {
            method: "get",
            path: "/a/b/c/d",
            mappingMatrix: {
              productType: "uni",
              actionType: "delete",
            },
          } as any
        }
        prefix={<span data-testid="apiMappingCardPrefix">Prefix</span>}
        suffix={<span data-testid="apiMappingCardSuffiix">Suffix</span>}
        onClick={handleClick}
      />
    );

    expect(getByTestId("apiMappingCardPrefix")).toHaveTextContent("Prefix");
    expect(getByTestId("apiMappingCardSuffiix")).toHaveTextContent("Suffix");

    expect(getByTestId("method")).toHaveTextContent("GET");
    expect(getByTestId("apiPath")).toHaveTextContent("/c/d");

    const mappingTypes = getAllByTestId("mappingType");
    const mappingValues = getAllByTestId("mappingValue");
    expect(mappingTypes).toHaveLength(2);
    expect(mappingValues).toHaveLength(2);

    expect(mappingTypes[0]).toHaveTextContent("product Type");
    expect(mappingValues[0]).toHaveTextContent("UNI");
    expect(mappingTypes[1]).toHaveTextContent("action Type");
    expect(mappingValues[1]).toHaveTextContent("DELETE");
  });

  it("should render api list loading skeleton", () => {
    const { container } = render(<ApiListSkeleton />);
    expect(container).toBeInTheDocument();
  });

  it("should render mapping version list loading skeleton", () => {
    const { container } = render(<ListVersionSkeleton />);
    expect(container).toBeInTheDocument();
  });

  it('should render api list', () => {
    const handleClick = vi.fn()
    const { getByTestId, getAllByTestId } = render(<ApiList
      loading
      title="mock_title"
      clickable
      highlights={{ 'mock_mapper_target': true }}
      indicators={['incomplete']}
      upgradeable={{ 'mock_mapper_target': true }}
      details={[{
        targetMapperKey: 'mock_mapper_target',
        mappingStatus: 'incomplete',
        path: '/a/b/c/d',
      }] as any}
      statusIndicatorPosition='right' onItemClick={handleClick} />)
    expect(getByTestId('mappingListTitle')).toHaveTextContent('mock_title')

    const useCase = getAllByTestId('useCase')[0]
    fireEvent.click(useCase)
    expect(handleClick).toHaveBeenCalledTimes(1)
  })

  it('should render skeleton loading deployment status component', () => {
    const { container } = render(<DeploymentStatus loading />)
    expect(container).toBeInTheDocument()
  })

  it('should render success deployment status component', async () => {
    vi.spyOn(userHooks, 'useUser').mockReturnValue({
      findUserName: () => 'admin'
    } as any)

    const { getByTestId, getByRole } = render(<DeploymentStatus deployment={{
      status: 'SUCCESS',
      envName: 'stage',
      createBy: 'admin',
      createAt: '2024-10-3 10:33:32'
    } as any} />)
    expect(getByTestId('deploymentEnv')).toHaveTextContent('stage')
    const infoIcon = getByTestId('stageDeploymentInfo')
    fireEvent.mouseEnter(infoIcon)
    await waitFor(() => expect(getByRole('tooltip')).toHaveTextContent('Deploy success.By admin2024-10-03 10:33:32'))
  })

  it('should render loading version select component', () => {
    const { container } = render(<VersionSelect isFetchingNextPage={true} hasNextPage={true} data={[]} setSelectedVersion={vi.fn()} selectedVersion={undefined} />)
    expect(container).toBeInTheDocument()
  })

  it('should render version select component', () => {
    const { container } = render(<VersionSelect isFetchingNextPage={false} hasNextPage={false} data={[]} setSelectedVersion={vi.fn()} selectedVersion={undefined} />)
    expect(container).toBeInTheDocument()
  })
});
