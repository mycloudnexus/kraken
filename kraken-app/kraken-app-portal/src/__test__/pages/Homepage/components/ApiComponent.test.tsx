import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import { IUnifiedAsset } from "@/utils/types/common.type";
import { BrowserRouter as Router } from 'react-router-dom';
import ApiComponent from '@/pages/HomePage/components/ApiComponent';

const mockOpenDrawer = vi.fn();
const mockNavigate = vi.fn();

vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
  BrowserRouter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

const defaultProps = {
  targetSpec: {
    metadata: {
      logo: 'mock-logo.png',
    },
  },
  supportInfo: 'UNI',
  apis: 3,
  title: 'Test API',
  targetYaml: {
    info: {
      description: '**Title** Description of the API',
    },
  },
  item: {
      metadata: {
          key: 'item-key',
          labels: { label1: 'Label 1', label2: 'Label 2' },
      },
  } as unknown as IUnifiedAsset,
  openDrawer: mockOpenDrawer,
};

describe('ApiComponent', () => {
  it('renders ApiComponent correctly', () => {
    render(
      <Router>
        <ApiComponent {...defaultProps} />
      </Router>
    );

    expect(screen.getByText('Test API')).toBeInTheDocument();
    expect(screen.getByText('3 APIs')).toBeInTheDocument();
    expect(screen.getByText('Label 1')).toBeInTheDocument();
    expect(screen.getByText('Label 2')).toBeInTheDocument();
    expect(screen.getByText('Description of the API')).toBeInTheDocument();
  });

  it('navigates to the correct URL when clicked', () => {
    render(
      <Router>
        <ApiComponent {...defaultProps} />
      </Router>
    );

    const apiComponent = screen.getByText('Test API').closest('div');
    fireEvent.click(apiComponent!);

    fireEvent.click(screen.getByText('Test API'));
    expect(mockNavigate).toHaveBeenCalledWith('/api-mapping/item-key', {
      state:{ productType: 'UNI' },
    });
  });
});
