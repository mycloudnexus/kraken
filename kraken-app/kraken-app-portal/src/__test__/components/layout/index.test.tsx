import { fireEvent, render, waitFor } from "@/__test__/utils"
import BasicLayout from "@/components/Layout/BasicLayout"
import * as hooks from '@/hooks/user'

beforeEach(() => {
  vi.clearAllMocks()
})

describe('layout and related component testing', () => {
  it('should render basic layout', async () => {
    vi.spyOn(hooks, 'useGetSystemInfo').mockReturnValue({
      data: {
        "id": "1bba3aa0-6f40-4974-8e77-59674227f783",
        "createdAt": "2024-10-15T05:14:56.363035Z",
        "updatedAt": "2024-10-29T07:01:21.530939Z",
        "controlProductVersion": "V1.0",
        "stageProductVersion": "V1.5.1",
        "productionProductVersion": "V1.5.1",
        "controlAppVersion": "0.285.0",  // control plane kraken version which will show up in the tooltip
        "productKey": "mef.sonata",
        "productName": "MEF LSO Sonata",
        "productSpec": "grace", // standard version
        "key": "CONTROL_PLANE",
        "status": "MAPPING",  //mapping template upgrading status, if it is not RUNNING, it means it is in upgrading process
        "productionAppVersion": "0.284.0",  // production data plane kraken version which will show up in the UI and tooltip
        "stageAppVersion": "0.284.0" //stage data plane kraken version which will show up in the tooltip
      },
      isFetching: false,
      isLoading: false,
      refetch: vi.fn()
    } as any)

    window.portalConfig.getCurrentAuthUser = () : any => {
      return {"name": "u"};
    };
    const { getByTestId } = render(<BasicLayout />)

    // Side bar navigation
    await waitFor(() =>
      expect(getByTestId('productionAppVersion')).toBeInTheDocument())
    expect(getByTestId('productionAppVersion')).toHaveTextContent('0.284.0')
    
    const appVersionsIndicator = getByTestId('appVersionsIndicator')
    expect(appVersionsIndicator).toBeInTheDocument()

    fireEvent.mouseEnter(appVersionsIndicator)
    await waitFor(() => expect(getByTestId('headline')).toBeInTheDocument())
    expect(getByTestId('vProductionAppVersion')).toHaveTextContent('0.284.0')
    expect(getByTestId('vStageAppVersion')).toHaveTextContent('0.284.0')
    expect(getByTestId('vControlPlaneAppVersion')).toHaveTextContent('0.285.0')

    // Header component
    await waitFor(() =>
      expect(getByTestId('logo')).toBeInTheDocument())
    expect(getByTestId('productName')).toHaveTextContent('MEF LSO Sonata')
    expect(getByTestId('productSpec')).toHaveTextContent('grace')

    expect(getByTestId('mappingInProgress')).toBeInTheDocument()

    const userAvatar = getByTestId('username')
    expect(userAvatar).toBeInTheDocument()

    fireEvent.mouseEnter(userAvatar)

    await waitFor(() =>
      expect(getByTestId('logoutOpt')).toBeInTheDocument())

    fireEvent.click(getByTestId('logoutOpt'))
  })
})