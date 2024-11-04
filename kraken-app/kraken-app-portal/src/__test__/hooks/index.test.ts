import { renderHook } from '@/__test__/utils'
import { useEnv } from '@/hooks/env'
import { useLongPolling } from '@/hooks/useLongPolling'
import { usePathQuery } from '@/hooks/usePathQuery'
import * as envHooks from '@/hooks/product'
import * as reactRouterDomHooks from 'react-router-dom'

beforeEach(() => {
  vi.clearAllMocks()
})

describe('Unit testing for custom hooks used throughtout the pages', () => {
  it.skip('should return path query hook', () => {
    vi.spyOn(reactRouterDomHooks, 'useLocation').mockReturnValue({
      search: 'a/b?c=d',
      state: undefined,
      key: '',
      pathname: '',
      hash: ''
    })

    const { result } = renderHook(usePathQuery)

    expect(result.current.size).toBe(1)

    const queryParam = result.current.get('c')
    expect(queryParam).toBe('d')
  })

  it('should render long polling hook', () => {
    const { result } = renderHook(() => useLongPolling({
      data: 'Hello',
      isLoading: false,
      isFetching: false,
      refetch: vi.fn(),
    } as any, 100))

    const { data, isLoading, isFetching, refetch } = result.current
    expect(data).toBe('Hello')
    expect(isLoading).toBe(false)
    expect(isFetching).toBe(false)

    refetch()
    expect(refetch).toHaveBeenCalledTimes(1)
  })

  it('should render use environment api hook', () => {
    vi.spyOn(envHooks, 'useGetProductEnvs').mockReturnValue({
      data: {
        data: [
          {
            id: '1',
            name: 'dev',
            createdAt: '11111',
            productId: '1',
          }
        ],
        total: 1,
        size: 10,
        page: 0,
      },
      isLoading: false,
      isFetching: false,
      isFetched: true,
    } as any)

    const { result } = renderHook(useEnv)

    const { environments, isLoading, findEnvByName } = result.current

    expect(environments?.data).toHaveLength(1)
    expect(environments?.data[0]).toEqual({
      id: '1',
      name: 'dev',
      createdAt: '11111',
      productId: '1',
    })

    expect(isLoading).toBe(false)
    
    const foundEnv = findEnvByName('dev')
    expect(foundEnv).toEqual({
      id: '1',
      name: 'dev',
      createdAt: '11111',
      productId: '1',
    })
  })
})