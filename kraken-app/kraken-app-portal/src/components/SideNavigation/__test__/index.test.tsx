import { render, screen, fireEvent, renderHook } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import SideNavigation from '../index';
import { useAppStore } from '@/stores/app.store';
import { queryClient } from "@/utils/helpers/reactQuery";
import { QueryClientProvider } from '@tanstack/react-query';

describe('SideNavigation', () => {
  beforeEach(() => {
    // Mock the useAppStore hook
    const { result } = renderHook(() =>
      useAppStore()
    );
    result.current.setCurrentProduct('testProduct')


  });

  test('renders the SideNavigation component', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <SideNavigation />
        </MemoryRouter>
      </QueryClientProvider>
    );

    // Check if the Home link is rendered
    expect(screen.getByText(/Home/i)).toBeInTheDocument();
  });

  test('shows the product branding when sider is not collapsed', () => {
    render(
      <QueryClientProvider client={queryClient}>

        <MemoryRouter>
          <SideNavigation />
        </MemoryRouter>
      </QueryClientProvider>

    );
  });

  test('does not show product branding when sider is collapsed', () => {
    render(
      <QueryClientProvider client={queryClient}>

        <MemoryRouter>
          <SideNavigation />
        </MemoryRouter>
      </QueryClientProvider>

    );

    // Collapse the sider
    const collapseButton = screen.getByRole('button');
    fireEvent.click(collapseButton);
  });
});
