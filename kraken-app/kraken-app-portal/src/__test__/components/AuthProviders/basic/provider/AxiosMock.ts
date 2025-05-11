import { vi } from "vitest";

function createMockedAxios() {  
  const mocks = vi.hoisted(() => ({
      post: vi.fn(),
  }));
  
  vi.mock('axios', async () => {
      const actual: any = await vi.importActual("axios");
      return {
          default: {
              ...actual.default,
              create: vi.fn(() => ({
                  ...actual.default.create(),
                  post: mocks.post,  
              })),
          },
      };
  });

  return mocks
}

const mockedAxios = createMockedAxios()

export { mockedAxios }