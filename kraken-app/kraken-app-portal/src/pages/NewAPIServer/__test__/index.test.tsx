import { fireEvent, getAllByTestId, render } from "@testing-library/react";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "@/utils/helpers/reactQuery";
import { BrowserRouter } from "react-router-dom";
import NewAPIServer from "..";
import SelectAPIServer from '../components/SelectAPIServer';
import { isURL } from '@/utils/helpers/url';
import { validateServerName, validateURL } from '@/utils/helpers/validators';
import { Mock } from 'vitest';

vi.mock('@/utils/helpers/url', () => ({
  isURL: vi.fn(),
}));

test("test API new", () => {
  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <NewAPIServer />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
});


test("test SelectApiServer", async () => {
  vi.mock("@/hooks/product", async () => {
    const actual = await vi.importActual("@/hooks/product");
    return {
      ...actual,
      useGetValidateServerName: vi.fn().mockResolvedValue({
        mutateAsync: vi.fn().mockReturnValue({
          data: false
        }),
        isLoading: false,
      }),
    };
  });

  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <SelectAPIServer />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
  const input = getAllByTestId(container, "api-seller-name-input")[0];
  const formContainer = getAllByTestId(container, "api-seller-name-container")[0];
  fireEvent.change(input, { target: { value: "test" } });
  expect(formContainer).toBeInTheDocument();
});

test("test SelectApiServer", async () => {
  vi.mock("@/hooks/product", async () => {
    const actual = await vi.importActual("@/hooks/product");
    return {
      ...actual,
      useGetValidateServerName: vi.fn().mockResolvedValue({
        mutateAsync: vi.fn().mockReturnValue({
          data: false
        }),
        isLoading: false,
      }),
    };
  });

  const { container } = render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <SelectAPIServer />
      </BrowserRouter>
    </QueryClientProvider>
  );
  expect(container).toBeInTheDocument();
  const input = getAllByTestId(container, "api-seller-name-input")[0];
  const formContainer = getAllByTestId(container, "api-seller-name-container")[0];
  fireEvent.change(input, { target: { value: "test" } });
  expect(formContainer).toBeInTheDocument();
});

it('should resolve if the URL is valid', async () => {
  (isURL as Mock).mockReturnValue(true);
  const result = await validateURL({}, 'http://valid-url.com');
  expect(result).toBeUndefined();
  expect(isURL).toHaveBeenCalledWith('http://valid-url.com');
});


it('should reject with an error if the URL is invalid', async () => {
  (isURL as Mock).mockReturnValue(false);
  await expect(validateURL({}, 'invalid-url')).rejects.toThrow('Please enter a valid URL');
  expect(isURL).toHaveBeenCalledWith('invalid-url');
});

it('should resolve if the server name is valid', async () => {
  const validateNameMock = vi.fn().mockResolvedValue({ data: true });
  const result = await validateServerName(validateNameMock, 'product-1', 'validName', 'name1');
  expect(result).toBeUndefined(); // Promise resolves without rejection
  expect(validateNameMock).toHaveBeenCalledWith({ productId: 'product-1', name: 'validName' });
});

it('should reject with an error message if the server name is taken', async () => {
  const validateNameMock = vi.fn().mockResolvedValue({ data: false });
  await expect(validateServerName(validateNameMock, 'product-1', 'takenName', 'name1')).rejects.toThrow('The name takenName is already taken');
  expect(validateNameMock).toHaveBeenCalledWith({ productId: 'product-1', name: 'takenName' });
});