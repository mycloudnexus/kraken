import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import ActivityDetailItem from '../index';
import { IActivityLog } from '@/utils/types/env.type';

const mockActivity: IActivityLog = {
  env: 'production',
  requestId: '1',
  uri: '/test-uri',
  path: '/test-path',
  method: 'GET',
  queryParameters: { param1: 'value1' },
  headers: { header1: 'value1' },
  request: { param1: 'value1', param2: 'value2' },
  response: { status: '200 OK', data: { key: 'value' } },
  createdAt: '2023-01-01T00:00:00Z',
  updatedAt: '2023-01-01T00:00:00Z',
  httpStatusCode: 200,
  requestIp: '127.0.0.1',
  responseIp: '127.0.0.1',
  callSeq: 1
};

const mockCollapseItems = (activity: IActivityLog) => ({
  parameterColumns: [
    { title: 'Parameter', dataIndex: 'parameter', key: 'parameter' },
    { title: 'Value', dataIndex: 'value', key: 'value' }
  ],
  parameterList: Object.entries(activity.request).map(([key, value]) => ({
    key,
    parameter: key,
    value: value as string
  }))
});

describe('ActivityDetailItem Component', () => {
  beforeAll(() => {
    Object.assign(navigator, {
      clipboard: {
        writeText: vi.fn().mockImplementation(() => Promise.resolve()),
      },
    });
  });

  test('renders the component with given props', () => {
    render(<ActivityDetailItem title="Test Activity" activity={mockActivity} collapseItems={mockCollapseItems} />);

    expect(screen.getByText('Test Activity')).toBeInTheDocument();
    expect(screen.getByText('GET')).toBeInTheDocument();
    expect(screen.getByText('/test-path')).toBeInTheDocument();
    expect(screen.getByText('Parameters')).toBeInTheDocument();
    expect(screen.getByText('Request body')).toBeInTheDocument();
    expect(screen.getByText('Response')).toBeInTheDocument();
  });

  test('handles "Copy all" click', async () => {
    render(<ActivityDetailItem title="Test Activity" activity={mockActivity} collapseItems={mockCollapseItems} />);

    const copyAllButtons = screen.getAllByText('Copy all');
    
    await act(async () => {
      fireEvent.click(copyAllButtons[0]);
      fireEvent.click(copyAllButtons[1]);
    });

    expect(navigator.clipboard.writeText).toHaveBeenCalledTimes(2);
    expect(navigator.clipboard.writeText).toHaveBeenCalledWith(JSON.stringify(mockActivity.request));
    expect(navigator.clipboard.writeText).toHaveBeenCalledWith(JSON.stringify(mockActivity.response));
  });

  test('changes "Copy all" text to "Copied!" on click and reverts after 3 seconds', async () => {
    render(<ActivityDetailItem title="Test Activity" activity={mockActivity} collapseItems={mockCollapseItems} />);

    const copyAllButton = screen.getAllByText('Copy all')[0];
    
    await act(async () => {
      fireEvent.click(copyAllButton);
    });

    await waitFor(() => expect(copyAllButton).toHaveTextContent('Copied!'));

    // Wait for the timeout to revert the text
    await waitFor(() => expect(copyAllButton).toHaveTextContent('Copy all'), { timeout: 3500 });
  });
});
